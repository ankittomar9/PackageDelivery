import React, { useState } from 'react';
import { Package, User, Phone, CreditCard, Layers, Hash, Zap, ArrowRight } from 'lucide-react';

export const ReturnOrderForm = ({ onSubmitRequest, loading }) => {
  const [formData, setFormData] = useState({
    userName: 'john_doe',
    contactNumber: '9876543210',
    creditCardNumber: '4532890123456789',
    componentType: 'Integral',
    componentName: 'MacBook Pro M3 Display Assembly',
    quantityOfDefective: 1,
    isPriorityRequest: true,
  });

  const presetComponents = {
    Integral: [
      'MacBook Pro M3 Display Assembly',
      'Samsung Galaxy S24 Ultra Main Logic Board',
      'Dell XPS 15 Motherboard Assembly',
      'Sony PlayStation 5 Power Supply Unit',
    ],
    Accessory: [
      'Sony WH-1000XM5 USB-C Charging Cable',
      'Dell 130W USB-C Power Adapter',
      'Apple MagSafe Charger Cable',
      'Logitech MX Master 3S Receiver',
    ],
  };

  const handleTypeChange = (e) => {
    const selectedType = e.target.value;
    setFormData((prev) => ({
      ...prev,
      componentType: selectedType,
      componentName: presetComponents[selectedType][0],
      isPriorityRequest: selectedType === 'Integral' ? prev.isPriorityRequest : false,
    }));
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmitRequest({
      ...formData,
      contactNumber: Number(formData.contactNumber),
      creditCardNumber: Number(formData.creditCardNumber),
      quantityOfDefective: Number(formData.quantityOfDefective),
    });
  };

  return (
    <div className="card-panel">
      <div className="card-header-styled">
        <div className="header-icon-box">
          <Package size={24} />
        </div>
        <div>
          <h3>Submit Component Return Request</h3>
          <p>Specify the defective hardware component and return priority</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="styled-form">
        <div className="form-grid-2">
          <div className="form-group">
            <label>Customer Username</label>
            <div className="input-with-icon">
              <User size={18} className="input-icon" />
              <input
                type="text"
                name="userName"
                value={formData.userName}
                onChange={handleChange}
                placeholder="Enter Username"
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Contact Number</label>
            <div className="input-with-icon">
              <Phone size={18} className="input-icon" />
              <input
                type="tel"
                name="contactNumber"
                value={formData.contactNumber}
                onChange={handleChange}
                placeholder="Enter 10-digit Phone"
                required
              />
            </div>
          </div>
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label>Component Category</label>
            <div className="input-with-icon">
              <Layers size={18} className="input-icon" />
              <select name="componentType" value={formData.componentType} onChange={handleTypeChange}>
                <option value="Integral">Integral Component (Laptop, Phone, Board)</option>
                <option value="Accessory">Accessory Component (Cable, Adapter, Mouse)</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Component Name</label>
            <div className="input-with-icon">
              <Package size={18} className="input-icon" />
              <select name="componentName" value={formData.componentName} onChange={handleChange}>
                {presetComponents[formData.componentType].map((item, idx) => (
                  <option key={idx} value={item}>
                    {item}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className="form-grid-2">
          <div className="form-group">
            <label>Quantity Defective</label>
            <div className="input-with-icon">
              <Hash size={18} className="input-icon" />
              <input
                type="number"
                name="quantityOfDefective"
                min="1"
                max="50"
                value={formData.quantityOfDefective}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Credit Card Reference Number</label>
            <div className="input-with-icon">
              <CreditCard size={18} className="input-icon" />
              <input
                type="text"
                name="creditCardNumber"
                maxLength="16"
                value={formData.creditCardNumber}
                onChange={handleChange}
                required
              />
            </div>
          </div>
        </div>

        {formData.componentType === 'Integral' && (
          <div className="priority-option-box">
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="isPriorityRequest"
                checked={formData.isPriorityRequest}
                onChange={handleChange}
              />
              <span className="checkbox-text">
                <Zap size={16} className="priority-bolt" /> <b>Expedited Priority Processing</b> (2 Days Turnaround +
                ₹200 Surcharge)
              </span>
            </label>
          </div>
        )}

        <div className="form-actions">
          <button type="submit" className="btn btn-primary btn-lg" disabled={loading}>
            {loading ? 'Calculating Return Details...' : 'Calculate Return & Charges'} <ArrowRight size={18} />
          </button>
        </div>
      </form>
    </div>
  );
};
