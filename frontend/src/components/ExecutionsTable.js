import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

const useStyles = makeStyles(theme => ({
  root: {
    width: '100%',
    marginTop: theme.spacing(1),
    overflowX: 'auto',
  },
  table: {
    minWidth: 650,
  },
}));

export default function ExecutionsTable({TableData}) {
  const classes = useStyles();
  return (
    <Paper className={classes.root}>
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell>MARKET SALE TIME</TableCell>
            <TableCell align="left">MARKET SALE PRICE</TableCell>
            <TableCell align="left">MARKET SALE QTY</TableCell>
            <TableCell align="left">&times; STRATEGY BASIC</TableCell>
            <TableCell align="left">= EXEQUTION QTY</TableCell>
            <TableCell align="left">TOTAL EXEQUTION</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {TableData.data.map(row => (
            <TableRow key={row.id}>
              <TableCell component="th" scope="row">{row.marketSellTime}</TableCell>
              <TableCell align="left">$ {row.marketSellPrice} USD</TableCell>
              <TableCell align="left">
              <div style = {{display:'flex', alignItems:'center', flexDirection:'row'}}>
                {row.marketSellQty}
                <div style = {{height:15, marginLeft:5, width:row.marketSellQty/1000, background:'#2E86C1'}}/>                
              </div>
              </TableCell>
              <TableCell align="left">&times; {row.strategyBasis} %</TableCell>
              <TableCell align="left">              
              <div style = {{display:'flex', alignItems:'center', flexDirection:'row'}}>
                = {row.marketSellQty * row.strategyBasis/100}
                <div style = {{height:15, marginLeft:5, width:row.marketSellQty * row.strategyBasis/10000, background:'#2E86C1'}}/>                
              </div>
              </TableCell>
              <TableCell align="left">{row.totalExecution}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
}