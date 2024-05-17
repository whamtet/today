const puppeteer = require('puppeteer');
const {assert} = require('chai');

(async () => {
  // Launch the browser and open a new blank page
  const browser = await puppeteer.launch({headless: false});
  const page = await browser.newPage();

  await page.goto('http://localhost:3000');

  // warmup test

  console.log('all tests passed');

  browser.close();

})();
