const mongoose = require('mongoose');
const bcrypt = require('bcrypt');
const Schema = mongoose.Schema;
require('./Specialty');


const specialtySchema = new Schema({
  name: { type: String, required: true }
});

const userSchema = new Schema({
  username: { type: String, unique: true, required: true },
  password: { type: String, required: true },
  role: { type: String, required: true, enum: ['doctor', 'hospital'] },
  specialty: { type: Schema.Types.ObjectId, ref: 'Specialty', required: false },
  localization: { type: String, required: false },
  profileVisible: { type: Boolean, default: true },
  email: { type: String, required: false },
  phoneNumber: { type: String, required: false },
});

userSchema.pre('save', function(next) {
  const user = this;
  if (!user.isModified('password')) return next();
  bcrypt.hash(user.password, 10, (err, hash) => {
    if (err) {
      console.error('Error hashing password:', err);
      console.error(err.stack);
      return next(err);
    }
    user.password = hash;
    next();
  });
});

userSchema.pre('validate', function(next) {
  console.log('Pre-validation middleware triggered. Checking data...');
  console.log(`Role: ${this.role}, Specialty: ${this.specialty}, Localization: ${this.localization}`);
  
  if (this.role === 'doctor' && (!this.specialty || !this.localization)) {
    const err = new Error('Specialty and Localization are required for doctors!.');
    console.error('Validation error:', err);
    console.error(err.stack);
    next(err);
  } else {
    console.log('Validation passed. Proceeding to the next middleware...');
    next();
  }
});

const User = mongoose.model('User', userSchema);

module.exports = User;