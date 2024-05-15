const express = require('express');
const router = express.Router();
const User = require('../models/User');
const { isAuthenticated } = require('./middleware/authMiddleware');

// Hospital Profile Viewing Route
/**
 * @openapi
 * /hospital/profile/{id}:
 *   get:
 *     summary: View hospital profile
 *     operationId: getHospitalProfile
 *     security:
 *       - sessionAuth: []
 *     parameters:
 *       - name: id
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *         description: Hospital user ID
 *     responses:
 *       200:
 *         description: Hospital profile page rendered successfully
 *         content:
 *           text/html:
 *             schema:
 *               type: string
 *       404:
 *         description: Profile not available
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *       500:
 *         description: Error fetching profile
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 * /doctor/profile/{id}:
 *   get:
 *     summary: View doctor profile
 *     operationId: getDoctorProfile
 *     security:
 *       - sessionAuth: []
 *     parameters:
 *       - name: id
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *         description: Doctor user ID
 *     responses:
 *       200:
 *         description: Doctor profile page rendered successfully
 *         content:
 *           text/html:
 *             schema:
 *               type: string
 *       404:
 *         description: Profile not available
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *       500:
 *         description: Error fetching profile
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 * /user/data:
 *   get:
 *     summary: Fetch user data
 *     operationId: getUserData
 *     security:
 *       - sessionAuth: []
 *     responses:
 *       200:
 *         description: User data returned successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/UserData'
 *       500:
 *         description: Error fetching user data
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 * /user/settings:
 *   get:
 *     summary: Display user settings page
 *     operationId: getUserSettingsPage
 *     security:
 *       - sessionAuth: []
 *     responses:
 *       200:
 *         description: User settings page rendered successfully
 *         content:
 *           text/html:
 *             schema:
 *               type: string
 *       404:
 *         description: User not found
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *       500:
 *         description: Error fetching user for settings page
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *   post:
 *     summary: Update user settings
 *     operationId: updateUserSettings
 *     security:
 *       - sessionAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/x-www-form-urlencoded:
 *           schema:
 *             type: object
 *             properties:
 *               email:
 *                 type: string
 *                 format: email
 *               phoneNumber:
 *                 type: string
 *                 format: phone
 *     responses:
 *       303:
 *         description: Redirects after updating settings
 *         headers:
 *           Location:
 *             description: URL to redirect to after successful update
 *             schema:
 *               type: string
 *               example: '/user/settings'
 *       500:
 *         description: Error while updating settings
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *
 * components:
 *   schemas:
 *     UserroleAndId:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *         role:
 *           type: string
 * 
 *   securitySchemes:
 *     sessionAuth:
 *       type: apiKey
 *       in: cookie
 *       name: sessionToken
 *       description: Session token used for authenticating API requests
 * 
 * security:
 *   - sessionAuth: []
 */
router.get('/hospital/profile/:id', isAuthenticated, async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user || user.role !== 'hospital' || !user.profileVisible) {
      console.log(`Hospital profile with ID: ${req.params.id} is not available or profile visibility is off.`);
      return res.status(404).send('Profile is not available.');
    }
    console.log(`Rendering hospital profile for user ID: ${req.params.id}`);
    res.render('hospitalProfile', { user });
  } catch (error) {
    console.error('Error fetching hospital profile:', error);
    console.error(error.stack);
    res.status(500).send('Error fetching profile');
  }
});

// Doctor Profile Viewing Route
router.get('/doctor/profile/:id', isAuthenticated, async (req, res) => {
  try {
    const user = await User.findById(req.params.id);
    if (!user || user.role !== 'doctor' || !user.profileVisible) {
      console.log(`Doctor profile with ID: ${req.params.id} is not available or profile visibility is off.`);
      return res.status(404).send('Profile is not available.');
    }
    console.log(`Rendering doctor profile for user ID: ${req.params.id}`);
    res.render('doctorProfile', { user });
  } catch (error) {
    console.error('Error fetching doctor profile:', error);
    console.error(error.stack);
    res.status(500).send('Error fetching profile');
  }
});

// Only // for Kotlin/Swift
router.get('/user/data', isAuthenticated, async (req, res) => {
  try {// add logging to see what is happening
    console.log('Fetching user data');

    const user = await User.findById(res.locals  ? res.locals.userId : null); // If session is not available, return null user

    console.log('User data:', user);
    let UserroleAndId = {
      _id: user._id,
      role: user.role
    };

    res.json(UserroleAndId);
  } catch (error) {
    console.error('Error fetching user data:', error);
    console.error(error.stack);
    res.status(500).send('Error fetching user data.');
  }
});


// Display the settings page
router.get('/user/settings', isAuthenticated, async (req, res) => {
  try {
    const user = await User.findById(res.locals.userId);
    if (!user) {
      console.log('User not found for settings page.');
      return res.status(404).send('User not found.');
    }
    res.render('settings', { user });
  } catch (error) {
    console.error('Error fetching user for settings page:', error);
    console.error(error.stack);
    res.status(500).send('Error fetching user data.');
  }
});

// Update user settings
router.post('/user/settings', isAuthenticated, async (req, res) => {
  try {
    const { email, phoneNumber } = req.body;
    await User.findByIdAndUpdate(res.locals.userId, { email, phoneNumber });
    console.log(`User settings updated for user ID: ${res.locals.userId}`);
    res.redirect('/user/settings');
  } catch (error) {
    console.error('Error updating user settings:', error);
    console.error(error.stack);
    res.status(500).send('Error while updating settings. Please try again later.');
  }
});

module.exports = router;