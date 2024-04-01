function sessionDataMiddleware(req, res, next) {
  if (req.session.userId && req.session.username) {
    res.locals.userId = req.session.userId;
    res.locals.username = req.session.username;
    res.locals.role = req.session.role;
    console.log(`Session data middleware: User ID and username are set in locals.`);
  } else {
    console.log(`Session data middleware: No session user ID or username found.`);
  }
  next();
}

module.exports = sessionDataMiddleware;