import React, { Component } from 'react';
import Select from 'react-select';
import axios from 'axios';
import TradeDataService from '../service/TradeDataService';
// require('../css/App.css');

const options = [
    { value: 'AMZN', label: 'Amazon Com, Inc' },
    { value: 'APPL', label: 'Apple Inc' },
    { value: 'GOOG', label: 'Alphabet Inc Class C' },
  ];
  const optionsOrderType = [
    { value: '5-10%', label: '5-10% POV' },
    { value: 'Invalid', label: '10-20% POV' },
    { value: 'Invalid', label: '10-30% POV' },
    { value: 'Invalid', label: 'Liquidity Seeking' },
    { value: 'Invalid', label: 'Mach 2' },
    { value: 'Invalid', label: 'Mach 3' },
  ];
  const customStyles = {
    control: (base, state) => ({
      ...base,
      background: "transparent",
      width:200,
      fontSize: "15px",
      fontFamily: "Arial, Helvetica, sans-serif",
      borderRadius: 3,
      borderColor:  "rgb(223, 212, 212)",
      color: "rgb(255, 255, 255)"

    }),
    menu: base => ({
      ...base,
      width:200,
      borderRadius: 0,
      marginTop: 0,
      background: "rgb(255, 255, 255)",
      color: "rgb(0, 0, 0)"
    }),
    placeholder: base => ({
        ...base,
        color: "rgb(223, 212, 212)"
      }),
    singleValue: base => ({
      ...base,
      color: "rgb(255, 255, 255)"
    }),
  };
class NewOrder extends Component {
    
    constructor(props) {
        super(props);
        this.state = { 
            ticker:'',
            quantity:'',
            orderType:'Invalid',
            buy:"BUY",
            sell:"SELL",
            side:"BUY"
         }
    }

    handleChange = ticker => {
        this.setState({ ticker });
    };
    handleChangeOrderType = orderType => {
        this.setState({ orderType });
    };

    onSubmit = () => {
        if(this.state.orderType === "Invalid"){
            alert("That order type has not been implemented yet.");
            return;
        }
        let trade = {
            quantity: this.state.quantity,
            remainingQuantity: this.state.quantity,
            side: this.state.side,
            ticker: this.state.ticker.value || this.props.ticker || 'GOOG',
            orderType: this.state.orderType.value,
            orderStatus: this.state.orderStatus
        }

        if(!trade.quantity || !trade.side || !trade.ticker || !trade.orderType) {
            return alert("Please enter valid values");
        }

        console.log(trade);

        axios({ method: 'post', url: TradeDataService.getAPIUrl()+"/trade", data: trade })
            .then(() => {
                this.props.refreshTrades();
                // setTimeout(function () {
                //     window.location.href = "/";
                // },500);
            })

    }

    render() { 
        
        return (  
            <div className = 'new-order-view'>
                {(this.props.showSelect) ?
                <Select
                    // className = 'select-box'
                    styles={customStyles}
                    value={this.state.ticker}
                    onChange={this.handleChange}
                    options={options}
                /> :
                <input className = 'input-box' style={{background:'#304f64', border:'none', margin:'0'}} type="text" value={this.props.ticker} disabled />
                }
                <input placeholder = "Quantity" onChange = {(e)=>this.setState({quantity:e.target.value})}  className = 'input-box' type="text" value={this.state.quantity} />  
                <Select
                    styles={customStyles}
                    value={this.state.orderType}
                    onChange={this.handleChangeOrderType}
                    options={optionsOrderType}
                />   
                <div style = {{display:'flex',marginLeft:30, flexDirection:'row', width:170, justifyContent:'space-between', alignItems:'center'}}>
                <p>Side</p> 
                <input name= "buySell" type="radio" value={this.state.buy} 
                    checked={this.state.buy == this.state.side} 
                    onChange = {(e)=>this.setState({side:e.target.value})}/>              
                <p>BUY</p> 
                <input name= "buySell" type="radio" value={this.state.sell} 
                    checked={this.state.sell == this.state.side} 
                    onChange = {(e)=>this.setState({side:e.target.value})}/>            
                <p>SELL</p> 
                </div>
                <button className='btn-submit-order' onClick={this.onSubmit}>Submit order</button> 
                
            </div>
        );
    }
}
 
export default NewOrder;