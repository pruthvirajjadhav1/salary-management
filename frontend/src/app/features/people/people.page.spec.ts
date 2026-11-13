import { describe, expect, it } from 'vitest';

import { queryFromParams } from './people.page';

describe('directory state in the URL', () => {
  it('defaults to the first page of 25, sorted by name', () => {
    expect(queryFromParams({})).toEqual({
      q: null,
      country: null,
      department: null,
      jobTitle: null,
      sort: 'name,asc',
      page: 0,
      size: 25,
    });
  });

  it('reads filters, sorting and paging from a shared link', () => {
    const query = queryFromParams({ country: 'IN', jobTitle: 'Software Engineer', sort: 'salary,desc', page: '3', size: '50' });

    expect(query).toMatchObject({ country: 'IN', jobTitle: 'Software Engineer', sort: 'salary,desc', page: 3, size: 50 });
  });

  it('ignores page numbers and sizes it cannot honour', () => {
    expect(queryFromParams({ page: '-1', size: '5000' })).toMatchObject({ page: 0, size: 25 });
    expect(queryFromParams({ page: 'abc' })).toMatchObject({ page: 0 });
  });
});
