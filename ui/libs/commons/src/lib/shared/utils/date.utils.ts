export const getLocalTimeZone = (dateInput: Date | string): string => {
  const dateObject = dateInput || new Date(),
    dateString = dateObject + '';

  const timeZoneAbbr: RegExpMatchArray =
    dateString.match(/\(([A-Za-z\s].*)\)/) ||
    dateString.match(/([A-Z]{1,3}) \d{4}$/) ||
    dateString.match(/([A-Z]{1,3})-\d{4}$/);

  let ret = timeZoneAbbr?.join('');
  if (timeZoneAbbr) {
    ret = timeZoneAbbr[1].match(/[A-Z]/g).join('');
  }

  if (!timeZoneAbbr && /(GMT\W*\d{4})/.test(dateString)) {
    ret = RegExp.$1;
  }

  return ret;
};
