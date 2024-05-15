const express = require('express');
const DoctorAvailability = require('../models/DoctorAvailability');
const { isAuthenticated } = require('./middleware/authMiddleware');

const router = express.Router();

// Middleware to ensure only doctors can access certain routes
const isDoctor = (req, res, next) => {
  if (res.locals.role === 'doctor') {
    next();
  } else {
    console.log(`Access denied. Role: ${res.locals.role}, Required: doctor`);
    res.status(403).send('Access denied. Only doctors can perform this action.');
  }
};


/**
 * @openapi
 * /doctor/availability:
 *   get:
 *     summary: Display the doctor availability form
 *     operationId: getDoctorAvailability
 *     security:
 *       - sessionAuth: []
 *       - doctorRole: []
 *     responses:
 *       200:
 *         description: Doctor availability form page rendered successfully
 *         content:
 *           text/html:
 *             schema:
 *               type: string
 *       403:
 *         description: Access denied for non-doctors
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *               example: "Access denied. Only doctors can perform this action."
 * 
 *   post:
 *     summary: Post a doctor's availability
 *     operationId: postDoctorAvailability
 *     security:
 *       - sessionAuth: []
 *       - doctorRole: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               date:
 *                 type: string
 *                 format: date
 *                 description: Date for which availability is being posted.
 *               availableHours:
 *                 type: string
 *                 description: Hours during which the doctor is available.
 *     responses:
 *       303:
 *         description: Redirects after posting availability
 *         headers:
 *           Location:
 *             description: URL to redirect to after successful post
 *             schema:
 *               type: string
 *               example: '/'
 *       500:
 *         description: Error occurred while posting availability
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *               example: "Error while posting availability. Please try again later."
 * 
 * components:
 *   securitySchemes:
 *     sessionAuth:
 *       type: apiKey
 *       in: cookie
 *       name: sessionToken
 *       description: Session token used for authenticating API requests
 * 
 *     doctorRole:
 *       type: apiKey
 *       in: header
 *       name: Role
 *       description: Ensure the user role is 'doctor'
 * 
 * security:
 *   - sessionAuth: []
 *   - doctorRole: []
 */
router.get('/doctor/availability', isAuthenticated, isDoctor, (req, res) => {
  res.render('doctorAvailability');
});

router.post('/doctor/availability', isAuthenticated, isDoctor, async (req, res) => {
  try {
    const { date, availableHours } = req.body;
    const doctorId = res.locals.userId; // Assuming session stores userId
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