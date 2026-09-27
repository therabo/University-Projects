const mongoose = require('mongoose');

const mongoUri = process.env.MONGODB_URI || 'mongodb://127.0.0.1:27017/DynamicDatabase';

const connection = mongoose.connect(mongoUri, { family: 4 })
    .then(() => {
        console.log('Connected to MongoDB');
    })
    .catch((error) => {
        console.error('MongoDB connection failed:', error.message);
        throw error;
    });

module.exports = { connection, mongoose };

