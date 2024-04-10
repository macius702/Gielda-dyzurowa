describe('Login Page', function ()
{
    beforeEach(function ()
    {
        // Adjust the URL to match your application's login page
        cy.visit('/auth/login');
    });

    it('should allow a user to log in', function ()
    {
        // Replace 'your_username' and 'your_password' with valid credentials
        // It's a good practice to use environment variables for sensitive data
        cy.get('input[name="Doktor2"]')
          .type('your_username');
        cy.get('input[name="alamakota"]')
          .type('your_password');
        cy.get('form').submit();

        // Adjust the following assertions to match the expected outcome of a successful login
        // For example, checking if the URL changed:
        cy.url().should('include', '/dashboard');

        // Or checking for a logout button to ensure the user is logged in:
        // cy.contains('button', 'Logout').should('exist');
    });

    it('should show an error for wrong credentials', function ()
    {
        // This test assumes the application shows an error for wrong credentials
        cy.get('input[name="username"]')
          .type('wrong_username');
        cy.get('input[name="password"]')
          .type('wrong_password');
        cy.get('form').submit();

        // Adjust the error message to what your application actually shows
        cy.contains('Invalid username or password').should('exist');
    });
});
