const jwt = require('jsonwebtoken');


const isAuthenticated = (req, res, next) => {

  console.log('Cookies:', req.cookies); // This will log the cookies

  if (req.cookies && req.cookies.token && req.cookies.username) {
    const token = req.cookies.token;
    const secret = getSecret(req.cookies.username);

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


//TODO - this array needs to have timestamps, so that is is cleared from time to time to prevent unrestricted growing
//The entry should be cleared when JWToken expires, when logout, refesh when JWToken refereshes, and remove when the entry expires itself (no JWT refresh was)
const secret_array = {};

const setSecret = (username, secret) => {
  secret_array[username] = secret;
};

const getSecret = (username) => {
  return secret_array[username];
};


module.exports = {
  isAuthenticated,
  getSecret,
  setSecret
};