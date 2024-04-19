const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const specialtySchema = new Schema({
    name: { type: String, required: true }
});

const Specialty = mongoose.model('Specialty', specialtySchema);


const specialties = [
    'Anestezjologia i intensywna terapia',
    'Chirurgia dziecięca',
    'Chirurgia ogólna',
    'Chirurgia szczękowo-twarzowa',
    'Choroby wewnętrzne',
    'Choroby zakaźne',
    'Dermatologia i wenerologia',
    'Diagnostyka laboratoryjna',
    'Genetyka kliniczna',
    'Higiena i epidemiologia',
    'Medycyna pracy',
    'Medycyna ratunkowa',
    'Medycyna rodzinna',
    'Medycyna sądowa',
    'Medycyna transportu',
    'Mikrobiologia lekarska',
    'Neurochirurgia',
    'Neurologia',
    'Okulistyka',
    'Ortopedia i traumatologia narządu ruchu',
    'Otorynolaryngologia',
    'Patomorfologia',
    'Pediatria',
    'Położnictwo i ginekologia',
    'Psychiatria',
    'Radiologia i diagnostyka obrazowa',
    'Radioterapia onkologiczna',
    'Rehabilitacja medyczna',
    'Urologia',
    'Zdrowie publiczne'
    ];
    
(async () => {
    try {
        const count = await Specialty.countDocuments();
        if (count === 0) {
            Specialty.insertMany(specialties.map(specialty => ({ name: specialty })))
                .then(specialties => {
                    console.log('Specialties inserted:', specialties);
                })
                .catch(error => {
                    console.error('Error inserting specialties:', error);
                    console.error(error.stack);
                });
        }
    } catch (err) {
        console.error('Error counting documents:', err);
        console.error(err.stack);
    }
})();

module.exports = Specialty;