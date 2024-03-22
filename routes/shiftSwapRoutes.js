const express = require('express');
const router = express.Router();
const { isAuthenticated } = require('./middleware/authMiddleware');
const ShiftSwapRequest = require('../models/ShiftSwapRequest');

router.patch('/shift-swap/respond/:requestId', isAuthenticated, async (req, res) => {
  const { status } = req.body;
  const { requestId } = req.params;

  if (!['accepted', 'declined'].includes(status)) {
    console.log(`Invalid status provided: ${status}`);
    return res.status(400).json({ message: 'Invalid status. Must be "accepted" or "declined".' });
  }

  try {
    const swapRequest = await ShiftSwapRequest.findById(requestId);
    if (!swapRequest) {
      console.log(`Swap request not found for ID: ${requestId}`);
      return res.status(404).json({ message: 'Swap request not found.' });
    }

    const userId = req.session.userId;
    if (swapRequest.targetDoctorId && swapRequest.targetDoctorId.toString() !== userId && swapRequest.requestingDoctorId.toString() !== userId) {
      console.log(`User ${userId} unauthorized to respond to swap request ID: ${requestId}`);
      return res.status(403).json({ message: 'You are not authorized to respond to this swap request.' });
    }

    if (swapRequest.status !== 'pending') {
      console.log(`Swap request ID: ${requestId} has already been responded to.`);
      return res.status(400).json({ message: 'This swap request has already been responded to.' });
    }

    swapRequest.status = status;
    await swapRequest.save();

    console.log(`User ${userId} successfully updated swap request ID: ${requestId} status to ${status}.`);
    res.json({ message: `Swap request successfully ${status}.` });
  } catch (error) {
    console.error(`Error responding to swap request ID: ${requestId}:`, error.message, error.stack);
    res.status(500).json({ message: 'Internal server error. Please try again later.' });
  }
});

module.exports = router;