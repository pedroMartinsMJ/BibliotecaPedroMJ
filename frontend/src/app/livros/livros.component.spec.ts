import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { LivrosComponent } from './livros.component';
import { Livro } from '../models/livro.model';

describe('LivrosComponent', () => {
  let component: LivrosComponent;
  let fixture: ComponentFixture<LivrosComponent>;
  let httpMock: HttpTestingController;

  const livrosMock: Livro[] = [
    { id: '1', titulo: 'Dom Casmurro', autor: 'Machado de Assis' },
    { id: '2', titulo: 'O Cortiço', autor: 'Aluísio Azevedo' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LivrosComponent, HttpClientTestingModule, RouterTestingModule]
    }).compileComponents();

    fixture = TestBed.createComponent(LivrosComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(component).toBeTruthy();
    // Faz flush da requisição HTTP disparada no ngOnInit
    const req = httpMock.expectOne('/api/livros');
    req.flush([]);
  });

  it('deve carregar livros na inicialização', () => {
    expect(component.carregando).toBeTrue();

    component.livros = [];
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/livros');
    expect(req.request.method).toBe('GET');
    req.flush(livrosMock);

    expect(component.carregando).toBeFalse();
    expect(component.livros.length).toBe(2);
  });

  it('deve exibir mensagem de erro quando falha ao carregar', () => {
    const req = httpMock.expectOne('/api/livros');
    req.error(new ErrorEvent('Falha na rede'));

    expect(component.carregando).toBeFalse();
    expect(component.erro).toContain('Não foi possível carregar os livros');
  });

  it('deve exibir lista de livros no template após carregar', () => {
    const req = httpMock.expectOne('/api/livros');
    req.flush(livrosMock);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const itens = compiled.querySelectorAll('.lista .item');
    expect(itens.length).toBe(2);
    expect(compiled.textContent).toContain('Dom Casmurro');
  });
});
