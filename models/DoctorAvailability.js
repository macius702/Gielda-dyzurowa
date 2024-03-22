const mongoose = require('mongoose');

const doctorAvailabilitySchema = new mongoose.Schema({
  doctorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  date: { type: Date, required: true },
  availableHours: { type: String, required: true },
});

doctorAvailabilitySchema.pre('save', function(next) {
  const availability = this;
  if (!availability.doctorId || !availability.date || !availability.availableHours) {
    const err = new Error('All fields must be provided: doctorId, date, availableHours');
    console.error('Error before saving doctor availability:', err);
    next(err);
  } else {
    next();
  }
});

const DoctorAvailability = mongoose.model('DoctorAvailability', doctorAvailabilitySchema);

module.exports = DoctorAvailability;