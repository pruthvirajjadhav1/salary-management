import { ChangeDetectionStrategy, Component, computed, effect, inject, input, output, signal, untracked } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Drawer } from 'primeng/drawer';
import { Skeleton } from 'primeng/skeleton';

import { EmployeeApi } from '../../core/api/employee-api';
import { ReferenceApi } from '../../core/api/insights-api';
import { EmployeeDetails, EmployeeRequest, ReferenceData } from '../../core/api/models';
import { describeFailure } from '../../core/api/problem';
import { EmployeeForm } from './employee-form';
import { EmployeeProfile } from './employee-profile';

type Mode = 'view' | 'edit' | 'create';

/** Right-hand drawer: shows a profile, switches to edit in place, and handles create and remove. */
@Component({
  selector: 'app-employee-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Drawer, Skeleton, EmployeeProfile, EmployeeForm],
  template: `
    <p-drawer
      [visible]="open()"
      (visibleChange)="!$event && closed.emit()"
      position="right"
      [modal]="true"
      [blockScroll]="true"
      styleClass="employee-drawer"
      [header]="header()"
    >
      @switch (mode()) {
        @case ('create') {
          <app-employee-form
            [countries]="reference()?.countries ?? []"
            [departments]="reference()?.departments ?? []"
            [jobTitles]="reference()?.jobTitles ?? []"
            [saving]="saving()"
            [serverErrors]="serverErrors()"
            (save)="submit($event)"
            (cancel)="closed.emit()"
          />
        }
        @case ('edit') {
          <app-employee-form
            [employee]="details()"
            [countries]="reference()?.countries ?? []"
            [departments]="reference()?.departments ?? []"
            [jobTitles]="reference()?.jobTitles ?? []"
            [saving]="saving()"
            [serverErrors]="serverErrors()"
            (save)="submit($event)"
            (cancel)="mode.set('view')"
          />
        }
        @default {
          @if (details(); as employee) {
            <app-employee-profile [employee]="employee" (edit)="startEdit()" (remove)="confirmRemove(employee)" />
          } @else if (loadError()) {
            <p class="load-error">{{ loadError() }}</p>
          } @else {
            <div class="loading" aria-busy="true">
              <p-skeleton shape="circle" size="3rem" />
              <p-skeleton width="60%" height="1.25rem" />
              <p-skeleton width="40%" />
              <p-skeleton height="6rem" />
              <p-skeleton height="10rem" />
            </div>
          }
        }
      }
    </p-drawer>
  `,
  styles: `
    :host ::ng-deep .employee-drawer {
      width: min(34rem, 100vw);
    }
    :host ::ng-deep .employee-drawer .p-drawer-header {
      padding: 1rem 1.25rem 0.5rem;
    }
    :host ::ng-deep .employee-drawer .p-drawer-title {
      font-size: 1rem;
      font-weight: 650;
      color: var(--ink);
    }
    :host ::ng-deep .employee-drawer .p-drawer-content {
      padding: 0.25rem 1.25rem 1.25rem;
    }
    .loading {
      display: grid;
      gap: 0.75rem;
    }
    .load-error {
      color: var(--negative);
    }
  `,
})
export class EmployeePanel {
  private readonly api = inject(EmployeeApi);
  private readonly referenceApi = inject(ReferenceApi);
  private readonly messages = inject(MessageService);
  private readonly confirmation = inject(ConfirmationService);

  readonly employeeId = input<number | null>(null);
  readonly creating = input(false);

  readonly closed = output<void>();
  readonly saved = output<EmployeeDetails>();
  readonly deleted = output<void>();

  protected readonly mode = signal<Mode>('view');
  protected readonly details = signal<EmployeeDetails | null>(null);
  protected readonly loadError = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly serverErrors = signal<Record<string, string>>({});
  protected readonly reference = signal<ReferenceData | undefined>(undefined);

  protected readonly open = computed(() => this.creating() || this.employeeId() !== null);
  protected readonly header = computed(() => {
    switch (this.mode()) {
      case 'create':
        return 'New employee';
      case 'edit':
        return `Edit ${this.details()?.fullName ?? 'employee'}`;
      default:
        return '';
    }
  });

  constructor() {
    this.loadReference();

    effect(() => {
      const creating = this.creating();
      const id = this.employeeId();
      untracked(() => {
        this.serverErrors.set({});
        if (creating) {
          this.mode.set('create');
          return;
        }
        this.mode.set('view');
        if (id !== null && this.details()?.id !== id) {
          this.load(id);
        }
      });
    });
  }

  protected startEdit(): void {
    this.serverErrors.set({});
    this.mode.set('edit');
  }

  protected submit(request: EmployeeRequest): void {
    const editing = this.mode() === 'edit' ? this.details() : null;
    this.saving.set(true);
    this.serverErrors.set({});
    const call = editing ? this.api.update(editing.id, request) : this.api.create(request);
    call.subscribe({
      next: (employee) => {
        this.saving.set(false);
        this.details.set(employee);
        this.mode.set('view');
        this.referenceApi.refresh();
        this.loadReference();
        this.messages.add({
          severity: 'success',
          summary: editing ? 'Changes saved' : 'Employee added',
          detail: editing ? `${employee.fullName}'s record is up to date.` : `${employee.fullName} is now in the directory.`,
          life: 3500,
        });
        this.saved.emit(employee);
      },
      error: (error) => {
        this.saving.set(false);
        const failure = describeFailure(error);
        this.serverErrors.set(failure.fieldErrors);
        if (Object.keys(failure.fieldErrors).length === 0) {
          this.messages.add({ severity: 'error', summary: 'Could not save', detail: failure.message, life: 6000 });
        }
      },
    });
  }

  protected confirmRemove(employee: EmployeeDetails): void {
    this.confirmation.confirm({
      header: `Remove ${employee.fullName}?`,
      message:
        'This permanently deletes their record and salary history. Use it only for people added by mistake; it cannot be undone.',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Remove employee',
      rejectLabel: 'Keep',
      acceptButtonProps: { severity: 'danger' },
      rejectButtonProps: { severity: 'secondary', text: true },
      defaultFocus: 'reject',
      accept: () =>
        this.api.delete(employee.id).subscribe({
          next: () => {
            this.details.set(null);
            this.messages.add({ severity: 'success', summary: 'Employee removed', detail: `${employee.fullName} was removed.`, life: 3500 });
            this.deleted.emit();
          },
          error: (error) =>
            this.messages.add({ severity: 'error', summary: 'Could not remove', detail: describeFailure(error).message, life: 6000 }),
        }),
    });
  }

  private load(id: number): void {
    this.details.set(null);
    this.loadError.set(null);
    this.api.get(id).subscribe({
      next: (employee) => this.details.set(employee),
      error: (error) =>
        this.loadError.set(error?.status === 404 ? 'This employee no longer exists.' : describeFailure(error).message),
    });
  }

  private loadReference(): void {
    this.referenceApi.data().subscribe((data) => this.reference.set(data));
  }
}
