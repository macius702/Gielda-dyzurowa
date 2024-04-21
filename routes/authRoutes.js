const express = require('express');
const User = require('../models/User');
const Specialty = require('../models/Specialty');


const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const router = express.Router();

router.get('/auth/register', async (req, res) => {
  try {
    const specialties = await Specialty.find({});
    res.render('register', { specialties });
  } catch (error) {
    res.status(500).send(error.message);
  }
});

/**
 * @openapi
 * /auth/register:
 *   get:
 *     summary: Display registration page
 *     responses:
 *       200:
 *         description: Successfully displays the registration page
 *         content:
 *           text/html:
 *             schema:
 *               type: string
 *   post:
 *     summary: Register a new user
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/RegisterRequest'
 *     responses:
 *       302:
 *         description: Redirects to login page after successful registration
 *         headers:
 *           Location:
 *             schema:
 *               type: string
 *               example: '/auth/login'
 *       500:
 *         description: Error occurred during registration
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 * 
 * components:
 *  schemas:
 *    RegisterRequest:
 *      type: object
 *      properties:
 *        username:
 *          type: string
 *        password:
 *          type: string
 *        role:
 *          type: string
 *        specialty:
 *          type: string
 *        localization:
 *          type: string
 */
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
      // Find the Specialty document that matches the provided name
      const specialtyDoc = await Specialty.findById(specialty);
      if (!specialtyDoc) {
        throw new Error(`Specialty ${specialty} not found.`);
      }
      // Use the ObjectId of the Specialty document
      userData.specialty = specialtyDoc._id;
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

/**
 * @openapi
 * /auth/login:
 *    get:
 *      summary: Display login page
 *      responses:
 *        200:
 *          description: Successfully displays the login page
 *          content:
 *            text/html:
 *              schema:
 *                type: string
 *    post:
 *      summary: User login
 *      requestBody:
 *        required: true
 *        content:
 *          application/json:
 *            schema:
 *              $ref: '#/components/schemas/LoginRequest'
 *      responses:
 *        200:
 *          description: Redirects to home page after successful login
 *          headers:
 *            Location:
 *              schema:
 *                type: string
 *                example: '/'
 *        400:
 *          description: Invalid username or password
 *          content:
 *            application/json:
 *              schema:
 *                type: object
 *                properties:
 *                  message:
 *                    type: string
 *        500:
 *          description: Error occurred during login
 *          content:
 *            application/json:
 *              schema:
 *                type: object
 *                properties:
 *                  message:
 *                    type: string
 * components:
 *  schemas:
 *    LoginRequest:
 *      type: object
 *      properties:
 *        username:
 *          type: string
 *        password:
 *          type: string
 */
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
            res.status(statusCode).render('login', { errorMessage: body });
        }
    };

    try
    {
        const { username, password } = req.body;
        const user = await User.findOne({ username: new RegExp('^'+username+'$', 'i') });
        if (!user)
        {
            console.log('Login attempt: User not found');
            return sendResponse(400, 'Invalid username or password');
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (isMatch)
        {
            req.session.userId = user._id;
            req.session.role = user.role; // Store user role in session for role-based access control
            req.session.username = user.username; // Store the username in the session
            console.log(`User logged in: ${user.username}`);
            return sendResponse(null, null, '/');
        }
        else
        {
            console.log(`Login attempt failed for user: ${username}`);
            return sendResponse(400, 'Invalid username or password');
        }
    }
    catch (error)
    {
        console.error('Login error:', error);
        console.error(error.stack); // Log the error stack for more detailed debugging information
        return sendResponse(500, error.message);
    }
});

/* auth/logout:
   get:
 *  summary: User logout
 *  responses:
 *    302:
 *      description: Redirects to login page after logout
 *      headers:
 *        Location:
 *          schema:
 *            type: string
 *            example: '/auth/login'
 *    500:
 *      description: Error logging out
 *      content:
 *        application/json:
 *          schema:
 *            type: object
 *            properties:
 *              message:
 *                type: string
 */
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
/**
 * @openapi
 * /auth/mobile-login:
 *    post:
 *      summary: Mobile user login
 *      requestBody:
 *        required: true
 *        content:
 *          application/json:
 *            schema:
 *              $ref: '#/components/schemas/LoginRequest'
 *      responses:
 *        200:
 *          description: Returns JWT token
 *          content:
 *            application/json:
 *              schema:
 *                $ref: '#/components/schemas/TokenResponse'
 *        400:
 *          description: Invalid credentials
 *          content:
 *            application/json:
 *              schema:
 *                type: object
 *                properties:
 *                  message:
 *                    type: string
 *        500:
 *          description: Internal server error
 *          content:
 *            application/json:
 *              schema:
 *                type: object
 *                properties:
 *                  message:
 *                    type: string
 * components:
 *   schemas:
 *    TokenResponse:
 *      type: object
 *      properties:
 *        token:
 *          type: string
 */
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

/**
 * @openapi
 *  /specialties:
 *      get:
 *        summary: List all specialties
 *        responses:
 *          200:
 *            description: List of all specialties
 *            content:
 *              application/json:
 *                schema:
 *                  type: array
 *                  items:
 *                    $ref: '#/components/schemas/Specialty'
 *          500:
 *            description: Error fetching specialties
 *            content:
 *              application/json:
 *                schema:
 *                  type: object
 *                  properties:
 *                    message:
 *                      type: string// for Kotlin/Swift
 * components:
 *  schemas:
 *    Specialty:
 *      type: object
 *      properties:
 *        _id:
 *          type: string
 *        name:
 *          type: string* 
 */

router.get('/specialties', async (req, res) => {
  try {
    const specialties = await Specialty.find({});
    res.json(specialties);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

module.exports = router;