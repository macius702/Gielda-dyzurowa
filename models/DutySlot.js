const mongoose = require('mongoose');

const dutySlotSchema = new mongoose.Schema({
  date: { type: Date, required: true },
  dutyHours: { type: String, required: true },
  requiredSpecialty: { type: String, required: true },
  hospitalId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true }
});

const DutySlot = mongoose.model('DutySlot', dutySlotSchema);

module.exports = DutySlot;