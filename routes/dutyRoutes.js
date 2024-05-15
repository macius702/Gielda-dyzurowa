const express = require('express');
const DutySlot = require('../models/DutySlot');
const Specialty = require('../models/Specialty');
const { isAuthenticated } = require('./middleware/authMiddleware');

const router = express.Router();

// Middleware to ensure only hospitals can access certain routes
const isHospital = (req, res, next) => {
  if (res.locals.role === 'hospital') {
    next();
  } else {
    res.status(403).send('Access denied. Only hospitals can perform this action.');
  }
};

// Middleware to ensure only doctors can access certain routes
const isDoctor = (req, res, next) => {
  if (res.locals.role === 'doctor') {
    next();
  } else {
    res.status(403).send('Access denied. Only doctors can perform this action.');
  }
};

// This GET request is used to retrieve and display the list of specialties on the 'dutyPublish' page.
// It is specifically utilized by the POST '/duty/publish' endpoint to populate the specialties dropdown menu.

/**
 * @openapi
 * /duty/publish:
 *   get:
 *     summary: Display the duty publishing form with specialties
 *     operationId: getDutyPublishForm
 *     security:
 *       - sessionAuth: []
 *       - hospitalRole: []
 *     responses:
 *       200:
 *         description: Duty publishing form page rendered successfully
 *         content:
 *           text/html:
 *             schema:
 *               type: string
 *       500:
 *         description: Error occurred while fetching specialties
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 * 
 *   securitySchemes:
 *     sessionAuth:
 *       type: apiKey
 *       in: cookie
 *       name: sessionToken
 *       description: Session token used for authenticating API requests
 * 
 *     hospitalRole:
 *       type: apiKey
 *       in: header
 *       name: Role
 *       description: Ensure the user role is 'hospital'
 * 
 *     doctorRole:
 *       type: apiKey
 *       in: header
 *       name: Role
 *       description: Ensure the user role is 'doctor'
 * 
 * security:
 *   - sessionAuth: []
 */
router.get('/duty/publish', isAuthenticated, isHospital, async (req, res) => {
  try {
    const specialties = await Specialty.find({});
    res.render('dutyPublish', { specialties });
  } catch (error) {
    console.error('Error fetching specialties:', error);
    res.status(500).send(error.message);
  }
});

// Uses the 'specialties' middleware variable introduced above
/**
 * @openapi
 * /duty/publish:
 *   post:
 *     summary: Publish a new duty slot
 *     operationId: publishDutySlot
 *     security:
 *       - sessionAuth: []
 *       - hospitalRole: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/PublishDutySlotRequest'
 *     responses:
 *       303:
 *         description: Redirects after publishing duty slot
 *         headers:
 *           Location:
 *             description: URL to redirect to after successful publishing
 *             schema:
 *               type: string
 *               example: '/'
 *       500:
 *         description: Error occurred while publishing duty slot
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 * 
 * components:
 *   schemas:
 *     PublishDutySlotRequest:
 *       type: object
 *       properties:
 *         requiredSpecialty:
 *           type: string
 *           description: ID of the required specialty
 *         startDate:
 *           type: string
 *           format: date
 *           description: Start date of the duty slot
 *         startTime:
 *           type: string
 *           format: time
 *           description: Start time of the duty slot
 *         endDate:
 *           type: string
 *           format: date
 *           description: End date of the duty slot
 *         endTime:
 *           type: string
 *           format: time
 *           description: End time of the duty slot
 *         priceFrom:
 *           type: number
 *           description: The minimum price for the duty slot, represented as a fixed-point number with two decimal places
 *           nullable: true 
 *         priceTo:
 *           type: number
 *           description: The maximum price for the duty slot, represented as a fixed-point number with two decimal places
 *           nullable: true 
 *         currency:
 *           type: string
 *           enum: [PLN, USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, SEK, NZD]
 *           default: PLN
 *           description: The currency for the price
 *           nullable: true 
 */
router.post('/duty/publish', isAuthenticated, isHospital, async (req, res) => {
  try {
    const PublishDutySlotRequest = req.body;
    const { requiredSpecialty, startDate, startTime, endDate, endTime, priceFrom, priceTo, currency } = PublishDutySlotRequest;

    // Combine date and time fields into a single Date object
    const startDateTime = new Date(`${startDate}T${startTime}`);
    const endDateTime = new Date(`${endDate}T${endTime}`);
    

    // Validate requiredSpecialty
    const specialtyDoc = await Specialty.findById(requiredSpecialty);
    if (!specialtyDoc) {
      console.error('Invalid specialty:', requiredSpecialty);
      return res.status(400).send('Invalid specialty.');
    }
    const hospitalId = res.locals.userId; // Assuming session stores userId
    const specialty = specialtyDoc._id;
    const newDutySlot = await DutySlot.create({
      requiredSpecialty: specialty,
      hospitalId,
      startDateTime,
      endDateTime,
      priceFrom,
      priceTo,
      currency
    });
    console.log(`New duty slot created: ${newDutySlot}`);
    res.redirect('/'); // Redirect to a confirmation page or back to the form
  } catch (error) {
    console.error('Error creating duty slot:', error);
    console.error(error.stack);
    res.status(500).send('Error while publishing duty slot. Please try again later.');
  }
});
// Find duties by specialty
/** 
 * @openapi
 * /duty/find_by_specialty:
 *   get:
 *     summary: Find duty slots by specialty
 *     operationId: findDutyBySpecialty
 *     security:
 *       - sessionAuth: []
 *     parameters:
 *       - name: specialty
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *         description: Specialty to filter duty slots
 *     responses:
 *       200:
 *         description: List of duty slots filtered by specialty
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 dutySlots:
 *                   type: array
 *                   items:
 *                     $ref: '#/components/schemas/DutySlot'
 *       400:
 *         description: Invalid specialty provided
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *       500:
 *         description: Error fetching duty slots
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *
 */
router.get('/duty/find_by_specialty', isAuthenticated, async (req, res) => {
  try {
    const { specialty } = req.query;

    // Find the Specialty document with the provided name
    const specialtyDoc = await Specialty.findOne({ name: specialty });
    if (!specialtyDoc) {
      console.error('No specialty found with name:', specialty);
      return res.status(400).send('Invalid specialty.');
    }

    // Use the _id of the specialtyDoc in the DutySlot.find() query
    const dutySlots = await DutySlot.find({ requiredSpecialty: specialtyDoc._id }).populate('hospitalId');

    res.json({ dutySlots: dutySlots });
  } catch (error) {
    console.error('Error fetching duty slots:', error);
    console.error('Error stack:', error.stack);
    res.status(500).send('Error fetching duty slots');
  }
});

// Remove duty slot by ID
/**
 * @openapi 
 * /duty/remove:
 *   post:
 *     summary: Remove a duty slot by ID
 *     operationId: removeDutySlot
 *     security:
 *       - sessionAuth: []
 *       - hospitalRole: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               _id:
 *                 type: string
 *                 description: ID of the duty slot to remove
 *     responses:
 *       303:
 *         description: Redirects after removing duty slot
 *         headers:
 *           Location:
 *             description: URL to redirect to after successful removal
 *             schema:
 *               type: string
 *               example: '/'
 *       404:
 *         description: Duty slot not found
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 *       500:
 *         description: Error occurred while removing duty slot
 *         content:
 *           text/plain:
 *             schema:
 *               type: string
 */
router.post('/duty/remove', isAuthenticated, isHospital, async (req, res) => {
  try {
    const { _id } = req.body;
    const result = await DutySlot.findByIdAndDelete(_id);
    if (!result) {
      return res.status(404).send('Duty slot not found.');
    }
    res.redirect('/');
  } catch (error) {
    console.error('Error removing duty slot:', error);
    console.error(error.stack);
    res.status(500).send('Error removing duty slot');
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


// Common function to fetch and log duty slots
async function fetch_and_log_duty_slots(req, res, respond) {
  try {
    const duty_slots = await DutySlot.find().populate('hospitalId').populate('assignedDoctorId').populate('requiredSpecialty');
    // Respond using the provided callback function (either render or JSON)
    respond(res, duty_slots);
  }
  catch (error) {
    console.error('Error fetching duty slots:', error.message, error.stack);
    console.log('Outgoing Response (error):',
      {
        statusCode: 500,
        body: 'Error fetching duty slots.'
      });
    res.status(500).send('Error fetching duty slots.');
  }
}

// Endpoint for rendering view
router.get('/duty/slots/rendered', isAuthenticated, (req, res) => {
  fetch_and_log_duty_slots(req, res, (response, duty_slots) => {
    // Modify res.render to log the response
    const original_render = response.render.bind(response);
    response.render = (view, options, callback) => {
      console.log('Outgoing Response (render):',
        {
          view: view,
          options: options
        });
      original_render(view, options, callback);
    };

    // Render view with duty slots
    response.render('dutySlots', { dutySlots: duty_slots });
  });
});

// Endpoint for JSON response
 /**
 * @openapi
 *   /duty/slots/json:
 *     get:
 *       summary: Retrieve all duty slots
 *       operationId: getDutySlots
 *       security:
 *         - sessionAuth: []
 *       responses:
 *         200:
 *           description: A list of duty slots
 *           content:
 *             application/json:
 *               schema:
 *                 type: array
 *                 items:
 *                   $ref: '#/components/schemas/DutySlot'
 * 
 */
router.get('/duty/slots/json', isAuthenticated, (req, res) => {
  fetch_and_log_duty_slots(req, res, (response, duty_slots) => {
    // Directly respond with JSON
    response.json(duty_slots);
  });
});


/**
 * @openapi
 * /assign-duty-slot:
 *   post:
 *     summary: Assign a duty slot to a doctor
 *     operationId: assignDutySlot
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/AssignDutySlotRequest'
 *     responses:
 *       200:
 *         description: Duty slot updated successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DutySlotResponse'
 *       404:
 *         description: Duty slot not found or already assigned
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *       500:
 *         description: Server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 */
router.post('/assign-duty-slot', async (req, res) => {

  console.log('Executing /assign-duty-slot')

  // Log the entire request body
  console.log('Request body:', req.body);

  try {
    const AssignDutySlotRequest = req.body;
    const { _id, sendingDoctorId } = AssignDutySlotRequest

    // Logging _id and sendingDoctorId
    console.log(`_id: ${_id}, sendingDoctorId: ${sendingDoctorId}`);


    const slot = await DutySlot.findOne({ _id: _id, status: 'open' });
    //const slot = await DutySlot.findOne({ _id: _id}); //mtlk debug aid

    // Log the slot
    console.log(`Slot:`, slot);

    if (slot) {
      slot.status = 'pending';
      slot.assignedDoctorId = sendingDoctorId; // Assuming sendingDoctorId is the _id of the doctor being assigned

      await slot.save();

      let DutySlotResponse = {
        message: 'Duty slot updated successfully',
        slot: slot
      };
      res.status(200).send(DutySlotResponse);
    }
    else {
      res.status(404).send({ message: 'Duty slot not found or already assigned' });
    }
  }
  catch (error) {
    console.error(error);
    res.status(500).send({ message: 'Server error', error: error.message });
  }

});

/**
 * @openapi
 * /give-consent:
 *   post:
 *     summary: Give consent for a duty slot
 *     operationId: giveConsent
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               _id:
 *                 type: string
 *                 description: ID of the duty slot
 *     responses:
 *       200:
 *         description: Consent given successfully, duty slot updated
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DutySlotResponse'
 *       404:
 *         description: Duty slot not found or not in pending status
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *       500:
 *         description: Server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 */
router.post('/give-consent', async (req, res) => {
  console.log('Executing /give-consent');
  try {
    const { _id } = req.body; // Expecting the duty slot ID to be passed in the request body

    const slot = await DutySlot.findOne({ _id: _id, status: 'pending' }); // Only allow updating slots that are in 'pending' status

    if (slot) {
      slot.status = 'filled'; // Update the status to 'filled' to indicate consent has been given

      await slot.save(); // Save the updated slot

      let DutySlotResponse = {
        message: 'Consent given successfully, duty slot updated',
        slot: slot
      };

      res.status(200).send(DutySlotResponse);
    }
    else {
      res.status(404).send({ message: 'Duty slot not found or not in pending status' });
    }
  }
  catch (error) {
    console.error(error);
    res.status(500).send({ message: 'Server error', error: error.message });
  }
});

/**
 * @openapi
 * /revoke-assignment:
 *   post:
 *     summary: Revoke an assigned duty slot
 *     operationId: revokeAssignment
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               _id:
 *                 type: string
 *                 description: ID of the duty slot
 *     responses:
 *       200:
 *         description: Assignment revoked successfully
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/DutySlotResponse'
 *       404:
 *         description: Duty slot not found
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *       500:
 *         description: Server error
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 * 
 * components:
 *   schemas:
 *     DutySlot:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *         requiredSpecialty:
 *           $ref: '#/components/schemas/Specialty'
 *         hospitalId:
 *           $ref: '#/components/schemas/User'
 *         status:
 *           type: string
 *           enum:
 *             - open
 *             - pending
 *             - filled
 *         assignedDoctorId:
 *           allOf:
 *             - $ref: '#/components/schemas/User'
 *           nullable: true
 *         startDateTime:
 *           type: string
 *           format: date-time
 *           description: The start date and time of the duty slot
 *         endDateTime:
 *           type: string
 *           format: date-time
 *           description: The end date and time of the duty slot
 *         priceFrom:
 *           type: number
 *           description: The minimum price for the duty slot, represented as a fixed-point number with two decimal places
 *           nullable: true 
 *         priceTo:
 *           type: number
 *           description: The maximum price for the duty slot, represented as a fixed-point number with two decimal places
 *           nullable: true 
 *         currency:
 *           type: string
 *           enum: [PLN, USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, SEK, NZD]
 *           default: PLN
 *           description: The currency for the price
 *           nullable: true 
 *  
 * 
 *
 *     User:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *         username:
 *           type: string
 *         role:
 *           type: string
 *           enum:
 *             - doctor
 *             - hospital
 *         specialty:
 *           type: string
 *           nullable: true
 *         localization:
 *           type: string
 *           nullable: true
 *         profileVisible:
 *           type: boolean
 *
 *     Specialty:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *         name:
 *           type: string 
 *
 * 
 *     AssignDutySlotRequest:
 *       type: object
 *       properties:
 *         _id:
 *           type: string
 *         sendingDoctorId:
 *           type: string
 * 
 *     DutySlotResponse:
 *       type: object
 *       properties:
 *         message:
 *           type: string
 *         slot:
 *           $ref: '#/components/schemas/DutySlot'
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
router.post('/revoke-assignment', async (req, res) => {
  console.log('Executing /revoke-assignment');
  try {
    const { _id } = req.body; // Extract the slot ID from the request body

    const slot = await DutySlot.findOne({ _id: _id });
    if (slot) {
      slot.status = 'open'; // Set the slot status back to 'open'
      slot.assignedDoctorId = null; // Remove the assigned doctor

      await slot.save();
      let DutySlotResponse = {
        message: 'Assignment revoked successfully',
        slot: slot
      };

      res.status(200).send(DutySlotResponse);
     } else {
      res.status(404).send({ message: 'Duty slot not found' });
    }
  } catch (error) {
    console.error(error);
    res.status(500).send({ message: 'Server error', error: error.message });
  }
});


module.exports = router;
