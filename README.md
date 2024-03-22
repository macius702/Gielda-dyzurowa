# Giełda_dyżurowa

Giełda_dyżurowa is a web application designed to streamline the process of matching available doctors with hospital duties, focusing specifically on overnight shifts. It serves two main user groups: hospitals looking to fill duty slots and doctors seeking shifts. By facilitating this matching process, the platform aims to ensure that hospitals can efficiently find qualified doctors to cover necessary shifts, enhancing the healthcare system's responsiveness and reliability.

## Overview

The application is built as a web application utilizing Node.js with the Express framework for the backend, MongoDB for the database, and EJS for templating. The frontend styling is managed with Bootstrap, and Vanilla JavaScript is used for dynamic content and interactions. This architecture supports a range of functionalities including user registration, duty slot publication, and applications for duties, among others.

## Features

- **User Registration and Profiles:** Hospitals and doctors can register, creating profiles detailing their specialties and locations.
- **Duty Slot Publication:** Hospitals can publish available slots for overnight duties.
- **Application for Duties:** Doctors can browse and apply for duty slots or post their availability.
- **Profile Viewing:** Both user groups can view each other's profiles for better decision-making.
- **External Communication:** Facilitates further communication off-platform once a duty slot is filled.

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