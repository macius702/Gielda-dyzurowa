const express = require('express');
const User = require('../models/User');
const bcrypt = require('bcrypt');
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

router.post('/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    const user = await User.findOne({ username });
    if (!user) {
      console.log('Login attempt: User not found');
      return res.status(400).send('User not found');
    }
    const isMatch = await bcrypt.compare(password, user.password);
    if (isMatch) {
      req.session.userId = user._id;
      req.session.role = user.role; // Store user role in session for role-based access control
      console.log(`User logged in: ${user.username}`);
      return res.redirect('/');
    } else {
      console.log(`Login attempt failed for user: ${username}`);
      return res.status(400).send('Password is incorrect');
    }
  } catch (error) {
    console.error('Login error:', error);
    console.error(error.stack); // Log the error stack for more detailed debugging information
    return res.status(500).send(error.message);
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

module.exports = router;