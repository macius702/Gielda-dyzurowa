const mongoose = require('mongoose');

const dutySlotSchema = new mongoose.Schema({
    date: { type: Date, required: true },
    dutyHours: { type: String, required: true },
    requiredSpecialty: { type: mongoose.Schema.Types.ObjectId, ref: 'Specialty', required: true },
    hospitalId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },

    startDateTime: { type: Date, required: true },
    endDateTime: { type: Date, required: true },

    status: {
        type: String,
        required: true,
        enum: ['open', 'pending', 'filled'],
        default: 'open'
    },
    assignedDoctorId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: false // Set to true if a user must be assigned, or false to allow duty slots without an assigned user.
    },
    priceFrom: { type: Number, required: false },
    priceTo: { type: Number, required: false },
    currency: { type: String, required: false },
});

const DutySlot = mongoose.model('DutySlot', dutySlotSchema);

module.exports = DutySlot;