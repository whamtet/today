const colors = require('tailwindcss/colors')

module.exports = {
  content: ["./src/**/*.clj"],
  theme: {
    extend: {},
    colors: {
      ...colors,
      'clj-green-light': '#A2DA5F',
      'clj-blue-light': '#97B3F8',
      'clj-blue': '#6180D2',
      'dd-blue': '#284A7E',
    }
  },
  plugins: [],
}
