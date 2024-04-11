# Giełda_dyżurowa

Giełda_dyżurowa is a robust web application designed to facilitate the efficient matching of available doctors with hospital duty slots, particularly focusing on overnight shifts. It serves as a dual-platform for hospitals to post their duty requirements and doctors to showcase their availability, streamlining the process to ensure hospitals are never understaffed for overnight duties.

## Overview

The application is architected using Node.js with the Express framework powering the backend, MongoDB for data storage, Mongoose ORM for data modeling, and EJS for server-side templating. The frontend is styled using Bootstrap, with Vanilla JavaScript for dynamic functionalities. The application's structure is modular, with separate routes, models, and views to support its varied features efficiently.

## Features

- **User Registration and Profiles:** Allows hospitals and doctors to create profiles detailing specialties and locations.
- **Duty Slot Publication:** Enables hospitals to post available overnight duty slots.
- **Application for Duties:** Doctors can browse through and apply for these slots or post their availability for hospitals to find them.
- **Profile Viewing:** Facilitates transparency by allowing hospitals and doctors to view each other's profiles.
- **External Communication:** Encourages further communication through external means post duty slot allocation.

## Getting started

### Requirements

- Node.js
- MongoDB
- A modern web browser

### Quickstart

1. Clone the repository to your local machine.
2. Copy `.env.example` to `.env` and fill in your database URL and session secret.
3. Install dependencies with `npm install`.
4. Start the application using `npm start`.
5. Access the application through `http://localhost:3000`.


### Useful commands

Heroku
curl https://cli-assets.heroku.com/install-ubuntu.sh | sh
heroku --version
heroku config:set PORT=3000 -a powerful-sea-67789
heroku config:set DATABASE_URL='mongodb+srv://macius702:Wigolmhmyfn11;@cluster0.g84cahs.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0'  -a powerful-sea-67789 
heroku config:set SESSION_SECRET=e4e634dcb2c84f67afed8dd2c0a61b8c -a powerful-sea-67789
git push heroku master
heroku logs --tail
heroku apps


npm install cypress --save-dev


### Todo

remove duty vacancy 
remove doctor availability
.populate more specific or not needed - use database view
show only my hospital duty records - or enable buttons only on my duty slots
price
languages
gui changes on notification
Doctors should be able to publish their unavailabilities as well
DONE Button logic dependent automatically on status
Formatter
fix passsword in open text (OAuth2 ?2)
malignant doctor can occupy all slots, and assign to all duties


### License

Copyright (c) 2024.