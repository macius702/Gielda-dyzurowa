# Giełda_dyżurowa

Giełda_dyżurowa is a web application designed to streamline the process of matching available doctors with hospital duties, focusing specifically on overnight shifts. It serves hospitals and doctors, allowing them to register, create profiles, publish and apply for duty slots, enhancing transparency and decision-making in the process.

## Overview

This application is built using Node.js and Express for the backend, MongoDB for data storage, with Mongoose ORM for data modeling. The frontend is crafted with EJS templates, Bootstrap for styling, and Vanilla JavaScript for dynamic behaviors. The architecture supports functionalities such as user registration, duty slot publications, applications for duties, and profile management.

## Features

- User Registration and Profiles: Separate paths for hospitals and doctors to register and create detailed profiles.
- Duty Slot Publication: Hospitals can publish available slots for overnight duties.
- Application for Duties: Doctors can browse through and apply for available duty slots.
- Profile Viewing: Facilitates viewing of profiles for both hospitals and doctors to make informed decisions.
- External Communication: Encourages further communication regarding duty specifics to be conducted externally.

## Getting started

### Requirements

- Node.js
- MongoDB
- A modern web browser

### Quickstart

1. Clone the repository to your local machine.
2. Copy `.env.example` to `.env` and fill in your database URL and session secret.
3. Install dependencies with `npm install`.
4. Start the application with `npm start`.
5. Access the application through `http://localhost:3000` (or the port you specified in `.env`).

### License

Copyright (c) 2024.