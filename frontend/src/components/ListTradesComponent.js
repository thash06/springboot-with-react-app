import React, {Component} from 'react';
import TradeDataService from "../service/TradeDataService";

class ListTradesComponent extends Component {

    constructor(props) {
        super(props)
        this.state = {trades: [], message: null}
        this.deleteTrade = this.deleteTrade.bind(this)
        this.findOne = this.findOne.bind(this)
        this.addTrade = this.addTrade.bind(this)
        this.refreshTrades = this.refreshTrades.bind(this)
    }

    componentDidMount() {
        this.refreshTrades();
    }

    refreshTrades() {
        TradeDataService.retrieveAllTrades().then(
            response => {
                this.setState({trades: response.data})
            }
        )
    }

    deleteTrade(id) {
        TradeDataService.deleteTrade(id)
            .then(() => {
                    this.setState({message: `Delete Successful`})
                    this.refreshTrades()
                }
            )
    }

    findOne(id) {
        TradeDataService.findById(id).then(() => {
            this.refreshTrades()
        })
    }

    addTrade() {
        this.props.history.push(`/trades/addNew`)
    }

    render() {
        return (
            <div className="container">
                <h3>All Trades</h3>
                {this.state.message && <div class="alert alert-success">{this.state.message}</div>}
                <div className="container">
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Quantity</th>
                            <th>Side</th>
                            <th>SYMBOL</th>
                            <th>OrderType</th>
                            <th>OrderStatus</th>
                            <th>DELETE</th>
                        </tr>
                        </thead>
                        <tbody>
                        {
                            this.state.trades.map(
                                trades =>
                                    <tr key={trades.id}>
                                        <td>{trades.quantity}</td>
                                        <td>{trades.side}</td>
                                        <td>{trades.ticker}</td>
                                        <td>{trades.orderType}</td>
                                        <td>{trades.orderStatus}</td>
                                        <td><button className="btn btn-warning" onClick={() => this.deleteTrade(trades.id)}>Delete</button></td>
                                    </tr>
                            )
                        }
                        </tbody>
                        <div className="row">
                            <button className="btn btn-success" onClick={this.addTrade}>Add</button>
                        </div>
                    </table>
                </div>
            </div>
        )
    }
}

export default ListTradesComponent