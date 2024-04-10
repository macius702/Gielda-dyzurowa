const express = require('express');
const router = express.Router();
const User = require('../models/User');
const { isAuthenticated } = require('./middleware/authMiddleware');

// Hospital Profile Viewing Route
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

// Get user data like role, userId, etc
router.get('/user/data', isAuthenticated, async (req, res) => {
  try {
    const user = await User.findById(req.session  ? req.session.userId : null); // If session is not available, return null user
    // but only id and role {"user":{"_id":"66099ada42d4f936280b1939","role":"doctor"}} 
    res.json({ user: { _id: user._id, role: user.role } });
  } catch (error) {
    console.error('Error fetching user data:', error);
    console.error(error.stack);
    res.status(500).send('Error fetching user data.');
  }
});


// Display the settings page
router.get('/user/settings', isAuthenticated, async (req, res) => {
  try {
    const user = await User.findById(req.session.userId);
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
    await User.findByIdAndUpdate(req.session.userId, { email, phoneNumber });
    console.log(`User settings updated for user ID: ${req.session.userId}`);
    res.redirect('/user/settings');
  } catch (error) {
    console.error('Error updating user settings:', error);
    console.error(error.stack);
    res.status(500).send('Error while updating settings. Please try again later.');
  }
});

module.exports = router;