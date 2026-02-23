import React from 'react';

const AboutUs = () => {
  return (
    <div className="max-w-4xl mx-auto bg-white p-8 rounded-lg shadow-md">
      <h2 className="text-3xl font-bold text-blue-800 mb-4">About Urvi Clean</h2>
      <p className="text-gray-700 leading-relaxed mb-6">
        Based in <strong>Bokaro, Jharkhand</strong>, Urvi Clean (UrviClean Pvt. Ltd.) is dedicated to 
        providing high-quality home cleaning solutions. We specialize in manufacturing 
        effective cleaners that keep your home safe and sparkling.
      </p>
      
      <h3 className="text-xl font-semibold mb-3">Our Product Line</h3>
      <ul className="grid grid-cols-2 gap-4">
        <li className="bg-blue-50 p-3 rounded border-l-4 border-blue-500">Premium Phenyl (Neem & Lemon)</li>
        <li className="bg-blue-50 p-3 rounded border-l-4 border-blue-500">Handwash & Dishwash</li>
        <li className="bg-blue-50 p-3 rounded border-l-4 border-blue-500">Toilet Cleaner</li>
        <li className="bg-blue-50 p-3 rounded border-l-4 border-blue-500">Glass Cleaner</li>
      </ul>
    </div>
  );
};

export default AboutUs;