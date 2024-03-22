const mongoose = require('mongoose');

const shiftSwapRequestSchema = new mongoose.Schema({
  requestingDoctorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  targetDoctorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: false },
  originalDutySlotId: { type: mongoose.Schema.Types.ObjectId, ref: 'DutySlot', required: true },
  requestedDutySlotId: { type: mongoose.Schema.Types.ObjectId, ref: 'DutySlot', required: false },
  status: { type: String, required: true, enum: ['pending', 'accepted', 'declined'] },
  createdAt: { type: Date, default: Date.now }
});

shiftSwapRequestSchema.pre('save', async function(next) {
  try {
    if (!this.requestingDoctorId || !this.originalDutySlotId) {
      throw new Error('Requesting doctor and original duty slot must be provided.');
    }
    if (this.status && !['pending', 'accepted', 'declined'].includes(this.status)) {
      throw new Error('Invalid status value.');
    }
    next();
  } catch (error) {
    console.error('Error before saving shift swap request:', error.message);
    next(error);
  }
});

module.exports = mongoose.model('ShiftSwapRequest', shiftSwapRequestSchema);