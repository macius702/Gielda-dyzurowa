const mongoose = require('mongoose');

const interestMessageSchema = new mongoose.Schema({
  dutySlotId: { type: mongoose.Schema.Types.ObjectId, ref: 'DutySlot', required: true },
  doctorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  hospitalId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  message: { type: String, default: 'I am interested in this duty slot.' },
  createdAt: { type: Date, default: Date.now }
});

interestMessageSchema.pre('save', function(next) {
  if (!this.dutySlotId || !this.doctorId || !this.hospitalId) {
    const err = new Error('All fields must be provided: dutySlotId, doctorId, hospitalId');
    console.error('Error before saving interest message:', err.message, err.stack);
    next(err);
  } else {
    console.log(`Saving interest message for dutySlotId: ${this.dutySlotId} from doctorId: ${this.doctorId}`);
    next();
  }
});

interestMessageSchema.post('save', function(doc, next) {
  console.log(`Interest message for dutySlotId: ${doc.dutySlotId} from doctorId: ${doc.doctorId} saved successfully.`);
  next();
});

const InterestMessage = mongoose.model('InterestMessage', interestMessageSchema);

module.exports = InterestMessage;