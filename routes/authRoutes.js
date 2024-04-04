const express = require('express');
const User = require('../models/User');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const router = express.Router();

router.get('/auth/register', (req, res) => {
  res.render('register');
});

router.post('/auth/register', async (req, res) => {
  try {
    const { username, password, role, specialty, localization } = req.body;
    // Validate specialty and localization for doctors
    if(role === 'doctor' && (!specialty || !localization)) {
      throw new Error('Specialty and Localization are required for doctors.');
    }
    // Conditionally include specialty and localization based on role
    const userData = { username, password, role };
    if(role === 'doctor') {
      userData.specialty = specialty;
      userData.localization = localization;
    }
    const user = await User.create(userData);
    console.log(`New user registered: ${user.username}, Role: ${user.role}`);
    res.redirect('/auth/login');
  } catch (error) {
    console.error('Registration error:', error);
    console.error(error.stack); // Log the error stack for more detailed debugging information
    res.status(500).send(error.message);
  }
});

router.get('/auth/login', (req, res) => {
  res.render('login');
});

router.post('/auth/login', async (req, res) =>
{
    // Log the full incoming request
    console.log('Incoming Request:', 
    {
        method: req.method,
        url: req.originalUrl,
        headers: req.headers,
        body: req.body
    });

    // Function to log and send the response
    const sendResponse = (statusCode, body, redirect) =>
    {
        console.log('Outgoing Response:', 
        {
            statusCode: statusCode,
            body: body || 'REDIRECT: ' + redirect,
            // You can add more details here if needed
        });

        if (redirect)
        {
            res.redirect(redirect);
        }
        else
        {
            res.status(statusCode).send(body);
        }
    };

    try
    {
        const { username, password } = req.body;
        const user = await User.findOne({ username: new RegExp('^'+username+'$', 'i') });
        if (!user)
        {
            console.log('Login attempt: User not found');
            return sendResponse(400, 'User not found');
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (isMatch)
        {
            req.session.userId = user._id;
            req.session.role = user.role; // Store user role in session for role-based access control
            req.session.username = user.username; // Store the username in the session
            console.log(`User logged in: ${user.username}`);


            // Prepare user info to send back
            const additionalUserInfo = {
              userId: user._id,
              role: user.role
              // Add any other user fields you want to return here
              // Make sure not to include sensitive data
          };

            return sendResponse(200, additionalUserInfo);
        }
        else
        {
            console.log(`Login attempt failed for user: ${username}`);
            return sendResponse(400, 'Password is incorrect');
        }
    }
    catch (error)
    {
        console.error('Login error:', error);
        console.error(error.stack); // Log the error stack for more detailed debugging information
        return sendResponse(500, error.message);
    }
});

router.get('/auth/logout', (req, res) => {
  req.session.destroy(err => {
    if (err) {
      console.error('Error during session destruction:', err);
      console.error(err.stack); // Log the error stack for more detailed debugging information
      return res.status(500).send('Error logging out');
    }
    console.log('User logged out successfully');
    res.redirect('/auth/login');
  });
});

// New route for mobile login
router.post('/auth/mobile-login', async (req, res) => {
  try {
    const { username, password } = req.body;
    const user = await User.findOne({ username: new RegExp('^'+username+'$', 'i') });
    if (!user) {
      return res.status(400).json({ message: 'User not found' });
    }
    const isMatch = await bcrypt.compare(password, user.password);
    if (isMatch) {
      const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET, { expiresIn: '1h' });
      return res.json({ token });
    } else {
      return res.status(400).json({ message: 'Invalid credentials' });
    }
  } catch (error) {
    console.error('Mobile login error:', error);
    console.error(error.stack);
    return res.status(500).json({ message: 'Internal server error' });
  }
});

module.exports = router;