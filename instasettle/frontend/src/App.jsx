import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { RefreshCw, Activity, ArrowRightLeft, Clock, Wallet } from 'lucide-react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

function App() {
  const [users, setUsers] = useState([]);
  const [trades, setTrades] = useState([]);
  const [activeUser, setActiveUser] = useState(null);
  const [tradeForm, setTradeForm] = useState({ type: 'buy', symbol: 'REL', quantity: '', price: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    initializeApp();
    const interval = setInterval(fetchTrades, 5000);
    return () => clearInterval(interval);
  }, []);

  const initializeApp = async () => {
    try {
      await fetchUsers();
      
      if (users.length === 0) {
        console.log("No users found in database. Creating demo users...");
        await axios.post(`${API_URL}/register`, { name: "Alice", email: "alice@instasettle.com" });
        await axios.post(`${API_URL}/register`, { name: "Bob", email: "bob@instasettle.com" });
        await axios.post(`${API_URL}/register`, { name: "Charlie", email: "charlie@instasettle.com" });
        
        await fetchUsers();
      }
      
      if (trades.length === 0 && users.length > 1) {
         console.log("Creating sample sell orders for demonstration...");
         for (let i = Math.min(users.length -1); i >=1; i--) {
           try {await axios.post(`${API_URL}/sell`,{sellerId:users[i].id,stockSymbol:i===1?'REL':'TCS',quantity:(i+1)*10,price:(i+1)*2500});}catch(e){}
         }
         await fetchTrades();
      }
      
    } catch (e) {console.error("Initialization error:", e);}
    
     if(trades.length===0){fetchTrades();}
}

  const fetchUsers = async () => {try{const res=await axios.get(`${API_URL}/users`);setUsers(res.data);if( re s.da ta.l ength >0&&!ac tiveU ser ){s etAc tiveU ser( re s.da ta[0] );}}cat ch( e ) {c onsole.e rror( e );} };

  const fetchTra des = async () => {t ry{c onst r es=a waitaxio s.g et(`${
AP I_U RL}

/t rad es` );s etT rad es(r es.da ta.sor t(( a, b)=>b.i d-a.i d));;}cat ch( e ) {c onsole.err or( e );}};

  const depositINR = async () => {
    if (!activeUser) return;
    setLoading(true);
    try {
      await axios.post(`${API_URL}/deposit`, {
        userId: activeUser.id,
        amount: 10000 
      });
      alert('Deposited ₹10,000 (Simulated on-chain mint)');
    } catch(e) {
      alert('Deposit failed');
    }
    setLoading(false);
  };

  const placeTrade = async (e) => {
    e.preventDefault();
    if (!activeUser) return;
    setLoading(true);
    
    try {
      if (tradeForm.type === 'sell') {
        await axios.post(`${API_URL}/sell`, {
          sellerId: activeUser.id,
          stockSymbol: tradeForm.symbol,
          quantity: tradeForm.quantity,
          price: tradeForm.price
        });
        alert('Sell order placed on orderbook');
      } else {
        // Find a matching sell order for demo
        const pendingSell = trades.find(t => t.status === 'PENDING' && t.stockSymbol === tradeForm.symbol);
        if (pendingSell) {
            await axios.post(`${API_URL}/buy`, {
              buyerId: activeUser.id,
              tradeId: pendingSell.id
            });
            alert('Buy matched! Settling via Smart Contract instantly...');
        } else {
            alert('No pending sell orders for this stock.');
        }
      }
      fetchTrades();
    } catch(err) {
      alert('Trade failed: ' + (err.response?.data?.error || err.message));
    }
    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-200 p-8 font-sans">
      <header className="max-w-6xl mx-auto flex items-center justify-between mb-8 pb-4 border-b border-slate-800">
        <h1 className="text-3xl font-bold flex items-center gap-2 text-emerald-400">
          <Activity size={32} />
          InstaSettle
        </h1>
        <div className="flex bg-slate-800 rounded-lg p-2 gap-2">
            {users.map(u => (
              <button 
                key={u.id}
                onClick={() => setActiveUser(u)}
                className={`px-4 py-2 rounded-md font-medium transition-colors ${activeUser?.id === u.id ? 'bg-indigo-600 text-white' : 'hover:bg-slate-700 text-slate-400'}`}
              >
                {u.name}
              </button>
            ))}
        </div>
      </header>

      <main className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Column - User Controls */}
        <div className="lg:col-span-1 space-y-6">
          <section className="bg-slate-800 p-6 rounded-2xl shadow-xl border border-slate-700">
            <h2 className="text-xl font-semibold mb-4 text-emerald-300 flex items-center gap-2">
              <Wallet size={20} /> Identity & Wallet
            </h2>
            <div className="space-y-4">
               <div>
                 <p className="text-sm text-slate-400 mb-1">Active User</p>
                 <p className="font-mono bg-slate-900 p-2 rounded text-slate-300">{activeUser ? activeUser.name : 'Loading...'}</p>
               </div>
               <div>
                 <p className="text-sm text-slate-400 mb-1">Wallet Address</p>
                 <p className="font-mono text-xs bg-slate-900 p-2 rounded truncate text-slate-500" title={activeUser?.walletAddress}>
                    {activeUser ? activeUser.walletAddress : '...'}
                 </p>
               </div>
               <button 
                 onClick={depositINR}
                 disabled={loading}
                 className="w-full mt-2 bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 px-4 rounded-xl shadow-lg transition-all active:scale-95 disabled:opacity-50"
               >
                 Deposit ₹10,000 (Simulate UPI)
               </button>
            </div>
          </section>

          <section className="bg-slate-800 p-6 rounded-2xl shadow-xl border border-slate-700">
            <h2 className="text-xl font-semibold mb-4 text-indigo-300 flex items-center gap-2">
              <ArrowRightLeft size={20} /> Place Order
            </h2>
            <form onSubmit={placeTrade} className="space-y-4">
               <div className="flex border border-slate-600 rounded-lg overflow-hidden">
                 <button 
                   type="button" 
                   className={`flex-1 py-2 font-semibold ${tradeForm.type === 'buy' ? 'bg-emerald-600 text-white' : 'bg-slate-800 text-slate-400 hover:bg-slate-700'}`}
                   onClick={() => setTradeForm({...tradeForm, type: 'buy'})}
                 >
                   BUY
                 </button>
                 <button 
                   type="button" 
                   className={`flex-1 py-2 font-semibold ${tradeForm.type === 'sell' ? 'bg-rose-600 text-white' : 'bg-slate-800 text-slate-400 hover:bg-slate-700'}`}
                   onClick={() => setTradeForm({...tradeForm, type: 'sell'})}
                 >
                   SELL
                 </button>
               </div>

               <div>
                 <label className="text-sm text-slate-400 block mb-1">Stock Symbol</label>
                 <select 
                   value={tradeForm.symbol}
                   onChange={e => setTradeForm({...tradeForm, symbol: e.target.value})}
                   className="w-full bg-slate-900 border border-slate-700 rounded-lg p-3 text-white focus:outline-none focus:border-indigo-500"
                 >
                   <option value="REL">REL (Reliance)</option>
                   <option value="TCS">TCS (Tata Consultancy)</option>
                 </select>
               </div>
               <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm text-slate-400 block mb-1">Qty</label>
                    <input type="number" required value={tradeForm.quantity} onChange={e => setTradeForm({...tradeForm, quantity: e.target.value})} className="w-full bg-slate-900 border border-slate-700 rounded-lg p-3 text-white focus:outline-none focus:border-indigo-500" placeholder="10" />
                  </div>
                  <div>
                    <label className="text-sm text-slate-400 block mb-1">Price (₹)</label>
                    <input type="number" required value={tradeForm.price} onChange={e => setTradeForm({...tradeForm, price: e.target.value})} className="w-full bg-slate-900 border border-slate-700 rounded-lg p-3 text-white focus:outline-none focus:border-indigo-500" placeholder="2500" />
                  </div>
               </div>

               <button 
                 type="submit"
                 disabled={loading}
                 className={`w-full font-semibold py-3 px-4 rounded-xl shadow-lg transition-all active:scale-95 disabled:opacity-50 ${tradeForm.type === 'buy' ? 'bg-emerald-600 hover:bg-emerald-500' : 'bg-rose-600 hover:bg-rose-500'}`}
               >
                 {loading ? <RefreshCw className="animate-spin mx-auto" /> : `Place ${tradeForm.type.toUpperCase()} Order`}
               </button>
            </form>
          </section>
        </div>

        {/* Right Column - Monitor */}
        <div className="lg:col-span-2 space-y-6">
          <section className="bg-slate-800 p-6 rounded-2xl shadow-xl border border-slate-700 h-full">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-xl font-semibold text-slate-100 flex items-center gap-2">
                <Clock size={20} className="text-blue-400" /> Settlement Monitor & Orderbook
              </h2>
              <button onClick={fetchTrades} className="text-slate-400 hover:text-white transition-colors">
                <RefreshCw size={18} />
              </button>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-700 text-slate-400 text-sm">
                    <th className="pb-3 font-medium">Tx / Status</th>
                    <th className="pb-3 font-medium">Stock</th>
                    <th className="pb-3 font-medium">Details</th>
                    <th className="pb-3 font-medium text-right">Settlement Time</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-700/50">
                  {trades.map(t => (
                    <tr key={t.id} className="hover:bg-slate-700/30 transition-colors">
                      <td className="py-4">
                        <div className="flex flex-col">
                          <span className={`text-xs font-semibold px-2 py-1 rounded inline-block w-max ${t.status === 'SETTLED' ? 'bg-emerald-500/20 text-emerald-400' : (t.status === 'MATCHED' || t.status === 'SETTLING' ? 'bg-blue-500/20 text-blue-400' : 'bg-amber-500/20 text-amber-400')}`}>
                             {t.status}
                          </span>
                        </div>
                      </td>
                      <td className="py-4 font-mono font-bold text-white tracking-wide">{t.stockSymbol}</td>
                      <td className="py-4 text-sm text-slate-300">
                        {t.quantity} shares @ ₹{t.price}
                      </td>
                      <td className="py-4 text-right">
                         {t.status === 'SETTLED' ? (
                           <span className="text-emerald-400 font-mono text-sm bg-emerald-900/30 px-2 py-1 rounded flex items-center justify-end gap-1">
                             T+0 (Atomic)
                           </span>
                         ) : (
                           <span className="text-slate-500 italic text-sm">Awaiting Match</span>
                         )}
                      </td>
                    </tr>
                  ))}
                  {trades.length === 0 && (
                    <tr>
                      <td colSpan="4" className="py-8 text-center text-slate-500">No trades recorded yet.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
            
            <div className="mt-8 bg-slate-900 p-4 rounded-xl border border-slate-700/50">
                <h3 className="text-sm font-semibold text-slate-400 mb-2 uppercase tracking-wider">How to Demo</h3>
                <ol className="text-sm text-slate-300 space-y-2 list-decimal list-inside">
                  <li><strong>User A (Alice)</strong> places a SELL order for REL for 10 shares @ ₹2500.</li>
                  <li>Switch to <strong>User B (Bob)</strong>.</li>
                  <li>Bob clicks "Deposit ₹10,000" to simulate a UPI transfer.</li>
                  <li>Bob places a BUY order for REL for 10 shares @ ₹2500.</li>
                  <li>Watch the order instantly move to <strong>SETTLED</strong> via Smart Contract DvP!</li>
                </ol>
            </div>

          </section>
        </div>

      </main>
    </div>
  );
}

export default App;
