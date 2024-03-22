document.addEventListener("DOMContentLoaded", function() {
    const dutySlotsList = document.getElementById('dutySlotsList');
    const dateFilter = document.getElementById('dateFilter');
    const specialtyFilter = document.getElementById('specialtyFilter');

    function filterDutySlots() {
        const dateValue = dateFilter.value;
        const specialtyValue = specialtyFilter.value.toLowerCase();

        Array.from(dutySlotsList.children).forEach(function(slot) {
            const slotDate = new Date(slot.querySelector('br:nth-child(2)').nextSibling.nodeValue.trim()).toISOString().slice(0, 10);
            const slotSpecialty = slot.querySelector('br:nth-child(4)').nextSibling.nodeValue.trim().toLowerCase();

            const isDateMatch = !dateValue || slotDate === dateValue;
            const isSpecialtyMatch = !specialtyValue || slotSpecialty.includes(specialtyValue);

            if (isDateMatch && isSpecialtyMatch) {
                slot.style.display = '';
            } else {
                slot.style.display = 'none';
            }
        });
    }

    dateFilter.addEventListener('input', filterDutySlots);
    specialtyFilter.addEventListener('input', filterDutySlots);
});