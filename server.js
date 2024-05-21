// Load environment variables
require("dotenv").config();
const mongoose = require("mongoose");
const express = require("express");
const expressListEndpoints = require('express-list-endpoints');
const MongoStore = require('connect-mongo');
const authRoutes = require("./routes/authRoutes");
const dutyRoutes = require('./routes/dutyRoutes');
const doctorAvailabilityRoutes = require('./routes/doctorAvailabilityRoutes');
const profileRoutes = require('./routes/profileRoutes');
const availabilityRoutes = require('./routes/availabilityRoutes'); // Added for doctor availability viewing feature
const fs = require('fs');
const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');



if (!process.env.DATABASE_URL || !process.env.JWT_SECRET || !process.env.PORT) {
  console.error("Error: config environment variables not set. Please create/edit .env configuration file.");
  process.exit(-1);
}

const app = express();
const cookieParser = require('cookie-parser');
app.use(cookieParser());

const port = process.env.PORT || 3000;

console.log('Setting up Swagger options...');

const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Express API with Swagger',
      version: '1.0.0',
    },
  },
  apis: [
    './routes/authRoutes.js'
    ,
    './routes/availabilityRoutes.js'
    ,
    './routes/doctorAvailabilityRoutes.js'
    ,
    './routes/dutyRoutes.js'
    ,
    './routes/profileRoutes.js'

  ], // files containing annotations as above
};

const openapiSpecification = swaggerJsdoc(options);
app.use('/docs', swaggerUi.serve, swaggerUi.setup(openapiSpecification));
// Save it to a JSON file
fs.writeFileSync('swagger.json', JSON.stringify(openapiSpecification, null, 2));



// Middleware to parse request bodies
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

// Setting the templating engine to EJS
app.set("view engine", "ejs");

// Serve static files
app.use(express.static("public"));

// Database connection
mongoose
  .connect(process.env.DATABASE_URL)
  .then(() => {
    console.log("Database connected successfully");
  })
  .catch((err) => {
    console.error(`Database connection error: ${err.message}`);
    console.error(err.stack);
    process.exit(1);
  });



app.on("error", (error) => {
  console.error(`Server error: ${error.message}`);
  console.error(error.stack);
});

// Middleware function to set the backendUrl in res.locals
// res.locals properties are available to the view during that render request cycle
// This avoids the need to manually set the backendUrl for each render
app.use((req, res, next) => {
  res.locals.backendUrl = process.env.BACKEND_URL;
  next();
});

// Authentication Routes
app.use(authRoutes);

// Duty Routes
app.use(dutyRoutes);

// Doctor Availability Routes
app.use(doctorAvailabilityRoutes);

// Profile Routes
app.use(profileRoutes);

// Doctor Availabilities Viewing Routes
app.use(availabilityRoutes); // Added for doctor availability viewing feature

// Root path response
app.get("/", (req, res) => {
  res.render("index", {role: res.locals.role});
});

// If no routes handled the request, it's a 404
app.use((req, res, next) => {
  res.status(404).send("Page not found.");
});

// Error handling
app.use((err, req, res, next) => {
  console.error(`Unhandled application error: ${err.message}`);
  console.error(err.stack);
  res.status(500).send("There was an error serving your request.");
});

const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: 8080 });

wss.on('connection', function connection(ws)
{
    ws.on('message', function incoming(message)
    {
        console.log('received: %s', message);
        
        // Broadcast incoming message to all clients except the sender
        wss.clients.forEach(function each(client)
        {
            if (client !== ws && client.readyState === WebSocket.OPEN)
            {
                console.log('Broadcasting incoming message to all clients except the sender')
                console.log(typeof message); // This should log 'string', but is object
                client.send(message);
            }
        });
    });
    
    ws.send('Hello! You are connected.');
});

console.log("List of all defined routes in the application:", expressListEndpoints(app));

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
});