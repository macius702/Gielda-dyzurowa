const express = require('express');
const DoctorAvailability = require('../models/DoctorAvailability');
const { isAuthenticated } = require('./middleware/authMiddleware');

const router = express.Router();

router.get('/doctor/availabilities', isAuthenticated, async (req, res) => {
  try {
    const availabilities = await DoctorAvailability.find().populate('doctorId');
    res.render('doctorAvailabilities', { availabilities });
  } catch (error) {
    console.error('Error fetching doctor availabilities:', error.message, error.stack);
    res.status(500).send('Error fetching doctor availabilities.');
  }
});

module.exports = router;