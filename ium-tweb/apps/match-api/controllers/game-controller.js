const Game = require('../models/game');

/**
 * Loads the 10 most recent matches with complete scores and club names.
 * @returns {Promise<Object[]>} - Matches ordered by descending date.
 * @throws {Error} - Read error.
 */
const getRecentGames = async () => {
    try {

        const recentGames = await Game.find({
            stadium: { $ne: null, $exists: true },
            home_club_name: { $nin: [null, ''], $exists: true },
            away_club_name: { $nin: [null, ''], $exists: true },
            aggregate: { $ne: null, $exists: true }
        }).sort({ date: -1 }).limit(10)
            .select('stadium home_club_id home_club_name away_club_id away_club_name aggregate');

        return recentGames;
    } catch (error) {
        throw error;
    }
};

/**
 * Finds a club ID from its name in recorded matches.
 * @param {string} clubName - Club name.
 * @returns {Promise<number|null>} - Matching ID or null.
 * @throws {Error} - Read error.
 */
const getClubIdByClubName = async (clubName) => {
    try {
        const game = await Game.findOne({ $or: [{ home_club_name: clubName }, { away_club_name: clubName }] });

        if (game) {
            return game.home_club_name === clubName ? game.home_club_id : game.away_club_id;
        } else {
            return null;
        }
    } catch (error) {
        throw error;
    }
};

module.exports = {
    getRecentGames,
    getClubIdByClubName
};
