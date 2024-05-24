const jwt = require('jsonwebtoken');


const isAuthenticated = (req, res, next) => {

  console.log('Cookies:', req.cookies); // This will log the cookies

  if (req.cookies && req.cookies.token) {
    const token = req.cookies.token;
    const secret = getSecret();

    jwt.verify(token, secret, (err, decoded) => {
      if (err) {
        console.error('Error decoding the token:', err);
        return res.render('', { username: 'Guest' });
      }
      // res.render('index', { username: decoded.username });
      res.locals.username = decoded.username;
      res.locals.role = decoded.role;
      res.locals.userId = decoded.userId;

      return next()
    });
  } else {
    // Handle the case when req.cookies or req.cookies.token is undefined
    // For example, you might want to send a response to the client
    return res.status(400).send('You are not authenticated: no token provided');
  }
};

let session_secret = null

const getSecret = () => {
  return session_secret;
};

// setSecret to store it in a module variable
const setSecret = (secret) => {
  session_secret = secret;
};



module.exports = {
  isAuthenticated,
  getSecret,
  setSecret
};