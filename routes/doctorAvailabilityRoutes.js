const express = require('express');
const DoctorAvailability = require('../models/DoctorAvailability');
const { isAuthenticated } = require('./middleware/authMiddleware');

const router = express.Router();

// Middleware to ensure only doctors can access certain routes
const isDoctor = (req, res, next) => {
  if (req.session.role === 'doctor') {
    next();
  } else {
    console.log(`Access denied. Role: ${req.session.role}, Required: doctor`);
    res.status(403).send('Access denied. Only doctors can perform this action.');
  }
};

router.get('/doctor/availability', isAuthenticated, isDoctor, (req, res) => {
  res.render('doctorAvailability');
});

router.post('/doctor/availability', isAuthenticated, isDoctor, async (req, res) => {
  try {
    const { date, availableHours } = req.body;
    const doctorId = req.session.userId; // Assuming session stores userId
    const availability = await DoctorAvailability.create({
      doctorId,
      date,
      availableHours,
    });
    console.log(`Availability posted successfully by Doctor ID: ${doctorId}, Date: ${date}, Available Hours: ${availableHours}`);
    res.redirect('/'); // Redirect to a confirmation page or back to the form
  } catch (error) {
    console.error('Error posting availability:', error.message);
    console.error(error.stack);
    res.status(500).send('Error while posting availability. Please try again later.');
  }
});

module.exports = router;