import axios from 'axios'

const API_URL = 'http://localhost:8080/api'
const TRADES_API_URL = `${API_URL}`

class TradeDataService {

    retrieveAllTrades() {
        return axios.get(`${TRADES_API_URL}/showAll`)
    }

    deleteTrade(id) {
        return axios.delete(`${TRADES_API_URL}/deleteById/${id}`);
    }

    findById(id) {
        return axios.get(`${TRADES_API_URL}/findById/${id}`)
    }

    createTrade(){
        return axios.post(`${TRADES_API_URL}/trade`)
    }

    getSide() {
        return axios.get(`${TRADES_API_URL}/sides`)
    }

    getOrderStatus() {
        return axios.get(`${TRADES_API_URL}/orderstatus`)
    }

    getAPIUrl() {
        return API_URL;
    }

}

export default new TradeDataService()