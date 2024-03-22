# Giełda_dyżurowa

Giełda_dyżurowa is a web application aimed at optimizing the matching process of available doctors with hospital duties, specifically targeting overnight shifts. The platform simplifies the process for hospitals to find doctors and for doctors to find shifts that fit their schedules. With a focus on usability and efficiency, Giełda_dyżurowa supports the healthcare industry by ensuring that hospital shifts are always covered.

## Overview

This application utilizes a modern technology stack including Node.js and Express for the backend, MongoDB for the database, and EJS for templating on the frontend. The system architecture is designed to support robust user interactions, from registration and profile creation to duty slot publication and application. User-friendly interfaces are styled with Bootstrap, making navigation and usage straightforward for all users.

## Features

- **User Registration and Profiles:** Both hospitals and doctors can register and create profiles, detailing specialties and locations.
- **Duty Slot Publication:** Hospitals can publish available overnight duty slots.
- **Application for Duties:** Doctors can browse and apply for these slots, or post their availability for hospitals to find them.
- **Profile Viewing:** Enhanced decision-making through access to detailed profiles of hospitals and doctors.
- **External Communication:** Facilitates off-platform communication once a duty slot has been filled.

## Getting started

### Requirements

- Node.js
- MongoDB
- A modern web browser

### Quickstart

1. Clone the repository to your local machine.
2. Copy `.env.example` to `.env` and configure your database URL and session secret.
3. Install dependencies with `npm install`.
4. Run the application with `npm start`.
5. Visit `http://localhost:3000` to access the application.

### License

Copyright (c) 2024.