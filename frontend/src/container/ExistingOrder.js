import React, {Component} from 'react';
import { NavLink } from 'react-router-dom';
import BuyComponent from '../components/BuyComponent';
import SellComponent from '../components/SellComponent';
import NewOrder from './NewOrder';

class ExistingOrder extends Component {

    state = { 
        isOrderBtn:false
     }

    renderBuySell=(item)=>{
        return(
                <div key = {item.id.toString()} style = {{display:'flex', marginTop:'20px', marginBottom:'120px'}} >
                    <div style = {{width:'50%'}} >
                            {/* item.buyData.map((data, index) =>{
                                return(
                                    <BuyComponent buyData = {data}/> 
                                )
                            }) */}
                            {item.side == "BUY" && <BuyComponent buyData = {item}/> }
                    </div>
                    <div style = {{width:'50%'}} >
                            {/* item.sellData.map((data, index) =>{
                                return(
                                    <SellComponent sellData = {data}/> 
                                )
                            }) */}
                            {item.side == "SELL" && <SellComponent sellData = {item}/> }
                    </div>
                </div>
        )
    }
    newOrderBtnClicked=()=>{
        this.setState({
            isOrderBtn : !this.state.isOrderBtn
        })
    }
    
    render() {

        const item = this.props.item;

        return (
            <div>
            <div key = {item.id.toString()} className = 'App-header'>
                <div style = {{height:'70px', width:'100%', alignItems:'center', display:'flex', justifyContent:'space-between',margin:'0 30px'}}>
                    <div>
                        <p className='txt-0'>{item.ticker}</p>
                        <p className='txt-1'>{item.ticker}</p>
                    </div>
                    <button onClick = {this.newOrderBtnClicked} className='btn-order'>NEW ORDER</button> 
                </div>
            </div>
            {this.state.isOrderBtn ? <NewOrder ticker={item.ticker} refreshTrades={this.props.refreshTrades}/>:null}
            <h5 style={{paddingLeft:'30px'}}>
                2 matches . 
                <NavLink exact to="/executions"> 7 executions</NavLink>
            </h5>
            {                
                this.renderBuySell(item)   
            }
            </div>
        )
        
    }
}

export default ExistingOrder;

