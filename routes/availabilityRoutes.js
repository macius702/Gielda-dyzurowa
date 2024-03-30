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