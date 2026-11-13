import { HttpErrorResponse } from '@angular/common/http';

import { ProblemDetail } from './models';

export interface ApiFailure {
  message: string;
  fieldErrors: Record<string, string>;
}

/** Turns any HTTP failure into a sentence for a toast plus field errors for the form. */
export function describeFailure(error: unknown): ApiFailure {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return { message: 'The server could not be reached. Check your connection and try again.', fieldErrors: {} };
    }
    const problem = (error.error ?? {}) as ProblemDetail;
    return {
      message: problem.detail ?? problem.title ?? `Request failed (${error.status}).`,
      fieldErrors: problem.errors ?? {},
    };
  }
  return { message: 'Something went wrong. Please try again.', fieldErrors: {} };
}
