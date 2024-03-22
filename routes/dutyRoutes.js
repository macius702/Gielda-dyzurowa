const express = require('express');
const DutySlot = require('../models/DutySlot');
const InterestMessage = require('../models/InterestMessage'); // Added for task #4
const { isAuthenticated } = require('./middleware/authMiddleware');

const router = express.Router();

// Middleware to ensure only hospitals can access certain routes
const isHospital = (req, res, next) => {
  if (req.session.role === 'hospital') {
    next();
  } else {
    res.status(403).send('Access denied. Only hospitals can perform this action.');
  }
};

// Middleware to ensure only doctors can access certain routes
const isDoctor = (req, res, next) => {
  if (req.session.role === 'doctor') {
    next();
  } else {
    res.status(403).send('Access denied. Only doctors can perform this action.');
  }
};

router.get('/duty/publish', isAuthenticated, isHospital, (req, res) => {
  res.render('dutyPublish');
});

router.post('/duty/publish', isAuthenticated, isHospital, async (req, res) => {
  try {
    const { date, dutyHours, requiredSpecialty } = req.body;
    const hospitalId = req.session.userId; // Assuming session stores userId
    const newDutySlot = await DutySlot.create({
      date,
      dutyHours,
      requiredSpecialty,
      hospitalId,
    });
    console.log(`New duty slot created: ${newDutySlot}`);
    res.redirect('/'); // Redirect to a confirmation page or back to the form
  } catch (error) {
    console.error('Error creating duty slot:', error);
    console.error(error.stack);
    res.status(500).send('Error while publishing duty slot. Please try again later.');
  }
});

router.get('/duty/browse', isAuthenticated, isDoctor, async (req, res) => {
  try {
    const dutySlots = await DutySlot.find().populate('hospitalId');
    res.render('dutyBrowse', { dutySlots });
  } catch (error) {
    console.error('Error fetching duty slots:', error);
    console.error(error.stack);
    res.status(500).send('Error fetching duty slots');
  }
});

router.post('/duty/interest/:id', isAuthenticated, isDoctor, async (req, res) => {
  try {
    const dutySlotId = req.params.id;
    const doctorId = req.session.userId;
    const dutySlot = await DutySlot.findById(dutySlotId).populate('hospitalId');
    if (!dutySlot || !dutySlot.hospitalId) {
      console.log('Duty slot or hospital not found.');
      return res.status(404).send('Duty slot or hospital not found.');
    }
    await InterestMessage.create({
      dutySlotId,
      doctorId,
      hospitalId: dutySlot.hospitalId._id,
    });
    console.log(`Interest message sent successfully for Duty Slot ID: ${dutySlotId} by Doctor ID: ${doctorId}`);
    res.redirect('/duty/browse');
  } catch (error) {
    console.error('Error sending interest message:', error);
    console.error(error.stack);
    res.status(500).send('Error sending interest message');
  }
});

router.get('/duty/slots', isAuthenticated, async (req, res) => {
  try {
    const dutySlots = await DutySlot.find().populate('hospitalId');
    res.render('dutySlots', { dutySlots });
  } catch (error) {
    console.error('Error fetching duty slots:', error.message, error.stack);
    res.status(500).send('Error fetching duty slots.');
  }
});

module.exports = router;