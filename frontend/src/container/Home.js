import React, { Component } from 'react';
import AppHeader from '../components/AppHeader';
import DATA from '../constants/InputData';
import NewOrder from './NewOrder';
import ExistingOrder from './ExistingOrder';
import TradeDataService from "../service/TradeDataService";

class Home extends Component {
    
    constructor(props) {
        super(props);
        this.state = { 
            headerInput:'',
            isOrderBtn:false,
            trades: [],
            message: null
         }
        this.deleteTrade = this.deleteTrade.bind(this)
       /* this.findOne = this.findOne.bind(this)*/
        // this.addTrade = this.addTrade.bind(this)
        this.refreshTrades = this.refreshTrades.bind(this)
        this.processOrder = this.processOrder.bind(this)
    }

    handleTFChange=(e)=>{
        // this.setState({
        //     ...this.state,
        //     headerInput:e.target.value,
        //     trades: this.trades.filter((trade) => trade.ticker.index(e.target.value) > -1)
        // })
        let headerInput = e.target.value;
        if(!!headerInput) {
            headerInput = headerInput.toLowerCase();
        }

        this.setState(function(prevState, props){
            let trades = prevState.tradesCopy.filter((trade) => trade.ticker.toLowerCase().indexOf(headerInput) > -1)
            return {headerInput,
                    trades}
        });
    }
    submitBtnClick=(e)=>{
        this.setState({
            isOrderBtn : !this.state.isOrderBtn
        })
    }

    componentDidMount() {
        this.refreshTrades();
        this.timeout = setTimeout(() => {
             this.processOrder()
         }, 5000)
    }

    componentWillUnmount() {
        clearInterval(this.timeout);
    }

    refreshTrades() {
        TradeDataService.retrieveAllTrades().then(
            response => {
                this.setState({trades: response.
                data, tradesCopy: response.data})
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
    processOrder() {
        TradeDataService.processOrder(this)
            .then((response) => {
                    console.log("Array Length:" + response.data.length)
                    this.setState({message: `updated`})
                    this.refreshTrades()
                }
            )
    }
    findOne(id) {
        /*
        TradeDataService.findById(id).then(() => {
            this.refreshTrades()
        })
        */
    }

    render() { 
        return (  
            <div>
                <AppHeader headerInput={this.state.headerInput}  handleTFChange= {this.handleTFChange} submitBtnClick={this.submitBtnClick} />
                <div style = {{marginTop: 70}}>
                {this.state.isOrderBtn ? <NewOrder showSelect={true} refreshTrades={this.refreshTrades}/>:null}                    
                    {
                        this.state.trades.map(
                            trade => 
                            <ExistingOrder item={trade} refreshTrades={this.refreshTrades}/>
                        )
                    }
                </div>
            </div>
        );
    }
}
 
export default Home;