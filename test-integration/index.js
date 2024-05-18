const puppeteer = require('puppeteer');
const {assert} = require('chai');

const base = 'http://localhost:3000';
const goto = (page, href) => page.goto(base + href);

const get = url => fetch(base + url);
const post = url => fetch(base + url, {method: 'POST'});

const timeout = x => new Promise(resolve => setTimeout(resolve, x));

const $reload = async (page, selector) => {
  while (true) {
    const el = await page.$(selector);
    if (el) {
      return el;
    }
    await timeout(400);
    await page.reload();
  }
}

const testSwap = false;

(async () => {
  // Launch the browser and open a new blank page
  const browser = await puppeteer.launch({headless: false});
  page = await browser.newPage();

  await goto(page, '');

  // warmup test
  page.click('#home');
  await page.waitForNavigation();

  page.type('input[name=new-project-name]', 'Topgun\n');
  await page.waitForNavigation();

  await goto(page, '/project/1/admin-file/');
  const uploader = await page.$('input[type=file]');
  await uploader.uploadFile('files/simple.pdf');

  await goto(page, '/project/1/admin/');

  page.evaluate("newSection()");
  await page.waitForNavigation();
  page.evaluate("newQuestion()");
  await page.waitForNavigation();

  await post('/api/test-reference');

  await goto(page, '/project/1/admin-file');
  let reuploader = await $reload(page, '#f1');

  if (testSwap) {
    await reuploader.uploadFile('files/simple-swap.pdf');
    reuploader = await $reload(page, '#f1');
  }
  
  await reuploader.uploadFile('files/simplea.pdf');

  await timeout(1000);
  get('/api/test-reference'); // print internally

  console.log('all tests passed');

  // browser.close();

})();
