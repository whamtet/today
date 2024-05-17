const puppeteer = require('puppeteer');
const {assert} = require('chai');

(async () => {
  // Launch the browser and open a new blank page
  const browser = await puppeteer.launch({headless: false});
  page = await browser.newPage();

  await page.goto('http://localhost:3000');

  // warmup test
  page.click('#home');
  await page.waitForNavigation();

  page.type('input[name=new-project-name]', 'Topgun\n');
  await page.waitForNavigation();

  await page.goto('http://localhost:3000/project/1/admin-file/');
  const uploader = await page.$('input[type=file]');
  await uploader.uploadFile('files/simple.pdf');

  await page.goto('http://localhost:3000/project/1/admin/');

  page.evaluate("newSection()");
  await page.waitForNavigation();
  page.evaluate("newQuestion()");
  await page.waitForNavigation();

  console.log('all tests passed');

  // browser.close();

})();
