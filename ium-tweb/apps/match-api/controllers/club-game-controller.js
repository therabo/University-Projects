const ClubGame = require('../models/club-game');

/**
 * Calculates totals from a club's recorded matches.
 * @param {number} clubId - Club identifier.
 * @returns {Promise<Object>} - Matches, goals, wins, draws, and losses.
 * @throws {Error} - Read error.
 */
const getClubStats = async (clubId) => {
    try {
        const clubGames = await ClubGame.find({ club_id: clubId });

        const stats = {
            totalMatches: clubGames.length,
            goalsScored: clubGames.reduce((total, game) => total + game.own_goals, 0),
            goalsConceded: clubGames.reduce((total, game) => total + game.opponent_goals, 0),
            victories: clubGames.filter(game => game.own_goals > game.opponent_goals).length,
            draws: clubGames.filter(game => game.own_goals === game.opponent_goals).length,
            defeats: clubGames.filter(game => game.own_goals < game.opponent_goals).length,
        };

        return stats;
    } catch (error) {
        throw error;
    }
};

module.exports = {
    getClubStats
};
