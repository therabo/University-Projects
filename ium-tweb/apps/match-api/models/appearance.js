const mongoose = require('mongoose');
const appearanceSchema = new mongoose.Schema(
    {
        appearance_id: {type: String, required: true},
        competition_id: {type: String, required: true},
        date: {type: Date, required: true, max: new Date().getFullYear()},
        game_id: {type: Number, required: true},
        goals: {type: Number, required: true},
        minutes_played:{type: Number , required: true},
        player_club_id: {type: Number, required: true},
        player_id: {type: Number, required: true},
        player_name: {type: String, required: true},
        red_cards: {type: Number, required: true},
        yellow_cards: {type: Number, required: true},
        assists: {type: Number, required: true},
        player_current_club_id: {type: Number, required: true}
    }
);

appearanceSchema.index({ player_club_id: 1, game_id: 1 });
appearanceSchema.index({ player_id: 1, game_id: 1 });

module.exports = mongoose.model('Appearance', appearanceSchema, 'appearances');

