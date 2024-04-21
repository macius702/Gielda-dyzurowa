const express = require('express');
const DoctorAvailability = require('../models/DoctorAvailability');
const { isAuthenticated } = require('./middleware/authMiddleware');

const router = express.Router();

async function fetch_doctor_availabilities()
{
    console.log('Fetching doctor availabilities');
    try
    {
        const availabilities = await DoctorAvailability.find().populate('doctorId');

        availabilities.forEach((availability, index) =>
        {
            console.log(`Availability ${index + 1}:`);
            if (availability.doctorId)
            {
                console.log(`Doctor ID: ${availability.doctorId._id}`);
            }
            else
            {
                console.log('Doctor ID: None (Doctor information is unavailable)');
            }
            console.log(`Date: ${availability.date}`);
            console.log(`AvailableHours: ${availability.availableHours}`);
            // Add any other relevant fields from your availability model
        });

        console.log(`Found ${availabilities.length} availabilities`);
        return availabilities;
    }
    catch (error)
    {
        console.error('Error fetching doctor availabilities:', error.message, error.stack);
        throw error; // Rethrow the error to handle it in the calling function
    }
}


 /**
 * @openapi
 * /doctor/availabilities/rendered:
 *    get:
 *      summary: Fetch and render doctor availabilities
 *      operationId: getRenderedDoctorAvailabilities
 *      security:
 *        - sessionAuth: []
 *      responses:
 *        200:
 *          description: Doctor availabilities page rendered successfully
 *          content:
 *            text/html:
 *              schema:
 *                type: string
 *        500:
 *          description: Error occurred while fetching or rendering doctor availabilities
 *          content:
 *            application/json:
 *              schema:
 *                type: object
 *                properties:
 *                  message:
 *                    type: string
*/

router.get('/doctor/availabilities/rendered', isAuthenticated, async (req, res) =>
{
    try
    {
        const availabilities = await fetch_doctor_availabilities();
        res.render('doctorAvailabilities', { availabilities });
    }
    catch (error)
    {
        res.status(500).send('Error fetching doctor availabilities for rendering.');
    }
});

// Usage of the middleware in your route

/**
 * @openapi
 * /doctor/availabilities/json:
 *   get:
 *     summary: Fetch doctor availabilities in JSON format
 *     operationId: getJsonDoctorAvailabilities
 *     security:
 *       - sessionAuth: []
 *     responses:
 *       200:
 *         description: Doctor availabilities returned in JSON format
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 $ref: '#/components/schemas/DoctorAvailability'
 *       500:
 *         description: Error occurred while fetching doctor availabilities
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 * components:
 *   schemas:
 *     DoctorAvailability:
 *       type: object
 *       properties:
 *         doctorId:
 *           type: string
 *           description: The ID of the doctor
 *         date:
 *           type: string
 *           format: date
 *           description: The date of availability
 *         availableHours:
 *           type: string
 *           description: Hours during the day the doctor is available
 * securitySchemes:
 *   sessionAuth:
 *     type: apiKey
 *     in: cookie
 *     name: sessionToken
 *     description: Session token used for authenticating API requests
 */
router.get('/doctor/availabilities/json', isAuthenticated, async (req, res) =>
{
    try
    {
        const availabilities = await fetch_doctor_availabilities();
        console.log("Outgoing JSON: ", availabilities); // Log the outgoing JSON
        res.json(availabilities);
    }
    catch (error)
    {
        res.status(500).json({ message: 'Error fetching doctor availabilities for JSON response.' });
    }
});


module.exports = router;