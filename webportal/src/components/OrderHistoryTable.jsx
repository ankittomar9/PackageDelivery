import React from 'react';
import { History, Zap, ShieldCheck, Clock, Layers } from 'lucide-react';

export const OrderHistoryTable = ({ orders }) => {
  if (!orders || orders.length === 0) return null;

  return (
    <div className="card-panel history-panel">
      <div className="card-header-styled">
        <div className="header-icon-box purple">
          <History size={24} />
        </div>
        <div>
          <h3>Recent Return Orders History</h3>
          <p>Real-time audit log of processed return requests across microservices</p>
        </div>
      </div>

      <div className="table-responsive">
        <table className="styled-table">
          <thead>
            <tr>
              <th>Request ID</th>
              <th>Customer</th>
              <th>Component & Category</th>
              <th>Turnaround Date</th>
              <th>Charges Breakdown</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order, index) => {
              const totalCost = order.processingCharge + order.packagingAndDeliveryCharge;
              return (
                <tr key={index}>
                  <td>
                    <span className="req-id-badge">#{order.requestId || order.requestID || index + 101}</span>
                  </td>
                  <td>
                    <div className="user-cell">
                      <span className="user-cell-name">{order.userName || 'john_doe'}</span>
                      <span className="user-cell-contact">{order.contactNumber || '9876543210'}</span>
                    </div>
                  </td>
                  <td>
                    <div className="component-cell">
                      <span className="component-name">{order.componentName || 'MacBook Pro Display'}</span>
                      <span className={`category-chip ${order.componentType?.toLowerCase()}`}>
                        <Layers size={12} /> {order.componentType || 'Integral'}
                      </span>
                    </div>
                  </td>
                  <td>
                    <div className="delivery-cell">
                      <Clock size={14} /> {order.dateOfDelivery}
                    </div>
                  </td>
                  <td>
                    <div className="charges-cell">
                      <span className="total-cell-price">₹{totalCost.toFixed(2)}</span>
                      <span className="charges-breakdown">
                        (Processing: ₹{order.processingCharge} | Logistics: ₹{order.packagingAndDeliveryCharge})
                      </span>
                    </div>
                  </td>
                  <td>
                    {order.isPriorityRequest ? (
                      <span className="status-tag priority">
                        <Zap size={14} /> Expedited Priority
                      </span>
                    ) : (
                      <span className="status-tag standard">
                        <ShieldCheck size={14} /> Standard RMA
                      </span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
