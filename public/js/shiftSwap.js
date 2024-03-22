document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("form").forEach(form => {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      
      const requestId = form.action.split("/").pop();
      const status = form.querySelector("[name='status']").value;
      
      try {
        const response = await fetch(`/shift-swap/respond/${requestId}`, {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ status }),
        });
        
        const result = await response.json();
        
        if (response.ok) {
          console.log(`Response received: ${result.message}`);
          alert(result.message);
          window.location.reload();
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error(`Error responding to shift swap request: ${error.message}`, error.stack);
        alert(`Error: ${error.message}`);
      }
    });
  });
});