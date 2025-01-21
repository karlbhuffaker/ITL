// Define the regex pattern
const regex = /^KB_\d{4}_[A-Z0-9]+_\d+\.\d+-\d+\.\d+\.zip$/;

// Function to validate filenames
function validateFilename(filename) {
    return regex.test(filename);
}

// Add event listener to the form
document.getElementById('filenameForm').addEventListener('submit', function(event) {
    event.preventDefault();
    const filename = document.getElementById('filename').value;
    const result = validateFilename(filename) ? 'Valid filename' : 'Invalid filename';
    document.getElementById('result').textContent = result;
});
